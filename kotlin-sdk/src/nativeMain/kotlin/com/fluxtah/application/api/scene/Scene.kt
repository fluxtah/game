package com.fluxtah.application.api.scene

import com.fluxtah.application.api.Application
import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.camera.Camera
import com.fluxtah.application.api.emitter.Emitter
import com.fluxtah.application.api.emitter.EmitterBehavior
import com.fluxtah.application.api.entity.CollisionResult2
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.sequence.Sequence
import com.fluxtah.application.api.sprite.SpriteBatch
import com.fluxtah.application.api.text.TextBatch
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef

val EMPTY_SCENE_INFO = SceneInfo(Scene.EMPTY)
var activeSceneInfo: SceneInfo = EMPTY_SCENE_INFO
private val sceneBuilders = mutableMapOf<String, () -> SceneInfo>()
private val scenes = mutableMapOf<String, SceneInfo>()

data class SceneInfo(
    val scene: BaseScene,
    val onCollision: ((scene: Scene, result: CollisionResult2) -> Unit)? = null,
    val onSceneCreated: ((Scene) -> Unit)? = null,
    val onSceneUpdate: OnSceneUpdate? = null,
    val onSceneBeforeUpdate: OnSceneBeforeUpdate? = null,
    val onSceneAfterUpdate: OnSceneAfterUpdate? = null
)

@OptIn(ExperimentalForeignApi::class)
data class EntityInfo(
    val entity: Entity,
    val onSceneEntityUpdate: OnSceneEntityUpdate? = null,
    val onSceneBeforeEntityUpdate: OnSceneBeforeEntityUpdate? = null,
    val onSceneAfterEntityUpdate: OnSceneAfterEntityUpdate? = null,
    val behaviors: List<EntityBehavior>,
    var stableRef: StableRef<EntityInfo>? = null
)

data class EntityPool(
    val entities: List<EntityInfo>,
    val entitiesAvailable: MutableList<EntityInfo>,
    val entitiesInUse: MutableList<EntityInfo>
)

data class EntityPoolInfo(
    var initialSize: Int,
    val factory: () -> EntityInfo,
    val entityPool: EntityPool
)

@OptIn(ExperimentalForeignApi::class)
data class EmitterInfo(
    val emitter: Emitter,
    val behaviors: List<EmitterBehavior>,
)

data class EmitterPoolInfo(
    val initialSize: Int,
    val factory: () -> EmitterInfo,
    val emittersAvailable: MutableList<EmitterInfo>,
    val emittersInUse: MutableList<EmitterInfo>,
)

data class SoundPoolInfo(
    val initialSize: Int,
    val factory: () -> Sound,
    val sounds: List<Sound>,
    val soundsAvailable: MutableList<Sound>,
    val soundsInUse: MutableList<Sound>,
)

@DslMarker
annotation class SceneDsl

interface Scene {
    fun setActiveCamera(id: String)
    fun activeCamera(): Camera?
    fun cameraById(id: String): Camera?

    fun entityById(id: String): Entity
    fun entityFromPool(id: String, block: (entity: Entity) -> Unit)
    fun entityToPool(entity: Entity)
    fun resetEntityPool(id: String)
    fun entityInPoolByCondition(poolId: String, condition: (entity: Entity) -> Boolean): Entity?
    fun spawnEntityFromPool(poolId: String): Entity
    fun emitterById(id: String): Emitter?
    fun emitterFromPool(id: String, block: (emitter: Emitter) -> Unit)
    fun emitterToPool(emitter: Emitter)
    fun resetEmitterPool(id: String)

    fun soundFromPool(id: String): Sound
    fun soundToPool(sound: Sound)

    fun createSequence(id: String): Sequence?

    fun textBatchById(id: String): TextBatch

    fun spriteBatchById(id: String): SpriteBatch?

    fun <T : SceneComponent> componentById(id: String): T

    fun entitiesInPool(id: String): List<Entity>
    fun entityPool(id: String): EntityPool

    fun <T> data(): T

    object EMPTY : BaseScene() {
        override fun setActiveCamera(id: String) = Unit
        override fun activeCamera(): Camera? = null
        override fun cameraById(id: String): Camera? = null

        override fun entityById(id: String): Entity = throw Exception("Entity with id $id does not exist")
        override fun entityFromPool(id: String, block: (entity: Entity) -> Unit) = Unit
        override fun entityToPool(entity: Entity) = Unit
        override fun resetEntityPool(id: String) = Unit
        override fun entityInPoolByCondition(poolId: String, condition: (entity: Entity) -> Boolean): Entity? = null
        override fun spawnEntityFromPool(id: String): Entity = throw Exception("Entity with id $id does not exist")

        override fun emitterById(id: String): Emitter? = null
        override fun emitterFromPool(id: String, block: (emitter: Emitter) -> Unit) = Unit
        override fun emitterToPool(emitter: Emitter) = Unit
        override fun resetEmitterPool(id: String) = Unit
        override fun soundFromPool(id: String): Sound = throw Exception("Sound with id $id does not exist")
        override fun soundToPool(sound: Sound) = Unit

        override fun createSequence(id: String): Sequence? = null

        override fun textBatchById(id: String): TextBatch = throw Exception("TextBatch with id $id does not exist")
        override fun spriteBatchById(id: String): SpriteBatch? = null
        override fun <T : SceneComponent> componentById(id: String): T =
            throw Exception("Component with id $id does not exist")

        override fun entitiesInPool(id: String): List<Entity> = emptyList()
        override fun entityPool(id: String): EntityPool = throw Exception("Entity pool with id $id does not exist")

        @Suppress("UNCHECKED_CAST")
        override fun <T> data(): T = 0 as T
    }
}

typealias OnSceneUpdate = ((scene: Scene, time: Float) -> Unit)
typealias OnSceneBeforeUpdate = ((scene: Scene, time: Float, deltaTime: Float) -> Unit)
typealias OnSceneAfterUpdate = ((scene: Scene, time: Float, deltaTime: Float) -> Unit)
typealias OnSceneEntityUpdate = ((scene: Scene, entity: Entity, time: Float) -> Unit)
typealias OnSceneBeforeEntityUpdate = ((scene: Scene, entity: Entity, time: Float, deltaTime: Float) -> Unit)
typealias OnSceneAfterEntityUpdate = ((scene: Scene, entity: Entity, time: Float, deltaTime: Float) -> Unit)

fun Application.scene(id: String, block: SceneBuilder.() -> Unit) {
    sceneBuilders[id] = {
        val builder = SceneBuilder(id)
        block(builder)
        builder.build()
    }
}

fun Application.setActiveScene(id: String) {
    val builder = sceneBuilders[id] ?: throw Exception("Scene with id $id does not exist")
    val existingScene = scenes[id]
    if (existingScene != null) {
        activeSceneInfo = existingScene
    } else {
        // Create new scene
        val sceneInfo = builder.invoke()

        // Only once if the scene is new
        sceneInfo.onSceneCreated?.invoke(sceneInfo.scene)

        //
        // Initialize components
        //
        sceneInfo.scene.components.forEach {
            it.value.scene = sceneInfo.scene
            it.value.initialize()
        }

        //
        // Initialize entities
        //
        sceneInfo.scene.entities.forEach {
            it.value.behaviors.forEach { behavior ->
                behavior.initialize()
            }
        }
        sceneInfo.scene.entityPools.forEach {
            it.value.entityPool.entitiesInUse.forEach { entityInfo ->
                entityInfo.behaviors.forEach { behavior ->
                    behavior.initialize()
                }
            }
        }

        //
        // Initialize emitters
        //
        sceneInfo.scene.emitters.forEach {
            it.value.behaviors.forEach { behavior ->
                behavior.initialize()
            }
        }
        sceneInfo.scene.emitterPools.forEach {
            it.value.emittersInUse.forEach { emitterInfo ->
                emitterInfo.behaviors.forEach { behavior ->
                    behavior.initialize()
                }
            }
        }

        //
        // Initialize cameras
        //
        sceneInfo.scene.cameras.forEach {
            it.value.behaviors.forEach { behavior ->
                behavior.initialize()
            }
        }

        sceneInfo.scene.components.values.forEach { component ->
            component.onSceneCreated()
        }

        // Set as active scene
        scenes[id] = sceneInfo
        activeSceneInfo = sceneInfo
    }
}

