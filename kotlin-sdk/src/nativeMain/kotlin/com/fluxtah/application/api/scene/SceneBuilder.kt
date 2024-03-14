package com.fluxtah.application.api.scene

import com.fluxtah.application.api.*
import com.fluxtah.application.api.camera.Camera
import com.fluxtah.application.api.camera.CameraBuilder
import com.fluxtah.application.api.emitter.EmitterBuilder
import com.fluxtah.application.api.emitter.EmitterPoolBuilder
import com.fluxtah.application.api.entity.EntityBuilder
import com.fluxtah.application.api.entity.EntityPoolBuilder
import com.fluxtah.application.api.entity.KotlinCollisionResult
import com.fluxtah.application.api.interop.c_createPhysics
import com.fluxtah.application.api.interop.model.CreatePhysicsInfo
import com.fluxtah.application.api.sequence.Sequence
import com.fluxtah.application.api.sequence.SequenceBuilder
import com.fluxtah.application.api.sprite.SpriteBatch
import com.fluxtah.application.api.sprite.SpriteBatchBuilder
import com.fluxtah.application.api.sprite.SpriteSheet
import com.fluxtah.application.api.sprite.SpriteSheetBuilder
import com.fluxtah.application.api.text.TextBatch
import com.fluxtah.application.api.text.TextBatchBuilder
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped

@SceneDsl
class SceneBuilder(val sceneId: String) {
    private val lights = mutableMapOf<String, (Scene) -> Light>()
    private val cameras = mutableMapOf<String, (Scene) -> Camera>()

    private val entities = mutableMapOf<String, (Scene) -> EntityInfo>()
    private val entityPools = mutableMapOf<String, (Scene) -> EntityPoolInfo>()

    private val emitters = mutableMapOf<String, (Scene) -> EmitterInfo>()
    private val emitterPools = mutableMapOf<String, (Scene) -> EmitterPoolInfo>()

    private val soundPools = mutableMapOf<String, (Scene) -> SoundPoolInfo>()
    private val sequences = mutableMapOf<String, () -> Sequence>()

    private var data: () -> Any = {}

    private val components = mutableMapOf<String, (Scene) -> SceneComponent>()

    private val spriteSheets = mutableMapOf<String, (Scene) -> SpriteSheet>()
    private val textBatches = mutableMapOf<String, (Scene) -> TextBatch>()
    private val spriteBatches = mutableMapOf<String, (Scene) -> SpriteBatch>()

    private var onSceneCreated: ((scene: Scene) -> Unit)? = null
    private var onSceneUpdate: OnSceneUpdate? = null
    private var onSceneBeforeUpdate: OnSceneBeforeUpdate? = null
    private var onSceneAfterUpdate: OnSceneAfterUpdate? = null
    private var onCollision: ((scene: Scene, result: KotlinCollisionResult) -> Unit)? = null

    private var gravityX: Float = 0.0f
    private var gravityY: Float = -9.81f
    private var gravityZ: Float = 0.0f

    fun gravity(x: Float, y: Float, z: Float) {
        gravityX = x
        gravityY = y
        gravityZ = z
    }

    fun <T : Any> data(block: () -> T) {
        data = block
    }

    fun component(id: String, block: (Scene) -> SceneComponent) {
        if (components.containsKey(id)) {
            throw Exception("Entity with id $id already exists")
        }
        components[id] = {
            block(it)
        }
    }

    fun spriteSheet(
        id: String,
        jsonFilePath: String,
        textureFilePath: String,
        builder: SpriteSheetBuilder.() -> Unit = {}
    ) {
        if (spriteSheets.containsKey(id)) {
            throw Exception("Sprite sheet with id $id already exists")
        }
        spriteSheets[id] = {
            SpriteSheetBuilder(it, id, jsonFilePath, textureFilePath).apply(builder).build()
        }
    }

    fun textBatch(id: String, spriteSheetId: String, builder: TextBatchBuilder.() -> Unit = {}) {
        if (textBatches.containsKey(id)) {
            throw Exception("Text batch with id $id already exists")
        }
        textBatches[id] = {
            TextBatchBuilder(it, id, spriteSheetId).apply(builder).build()
        }
    }

    fun spriteBatch(id: String, spriteSheetId: String, builder: SpriteBatchBuilder.() -> Unit = {}) {
        if (spriteBatches.containsKey(id)) {
            throw Exception("Sprite batch with id $id already exists")
        }
        spriteBatches[id] = {
            SpriteBatchBuilder(it, id, spriteSheetId).apply(builder).build()
        }
    }

    fun light(id: String, builder: LightBuilder.() -> Unit) {
        if (lights.containsKey(id)) {
            throw Exception("Entity with id $id already exists")
        }
        lights[id] = {
            LightBuilder().apply(builder).build()
        }
    }

    fun camera(id: String, builder: CameraBuilder.() -> Unit) {
        if (cameras.containsKey(id)) {
            throw Exception("Entity with id $id already exists")
        }
        cameras[id] = {
            CameraBuilder(it).apply(builder).build()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun entity(id: String, modelPath: String, builder: EntityBuilder.() -> Unit) {
        if (entities.containsKey(id)) {
            throw Exception("Entity with id $id already exists")
        }
        entities[id] = {
            EntityBuilder(it, id, modelPath).apply(builder).build()
        }
    }

    fun entityPool(id: String, modelPath: String, builder: EntityPoolBuilder.() -> Unit) {
        if (entityPools.containsKey(id)) {
            throw Exception("Entity pool with id $id already exists")
        }
        entityPools[id] = {
            EntityPoolBuilder(it, id, modelPath).apply(builder).build()
        }
    }

    fun emitter(id: String, builder: EmitterBuilder.() -> Unit) {
        if (emitters.containsKey(id)) {
            throw Exception("Emitter with id $id already exists")
        }
        emitters[id] = {
            EmitterBuilder(it, id).apply(builder).build()
        }
    }

    fun emitterPool(id: String, builder: EmitterPoolBuilder.() -> Unit) {
        if (emitterPools.containsKey(id)) {
            throw Exception("Emitter pool with id $id already exists")
        }
        emitterPools[id] = {
            EmitterPoolBuilder(it, id).apply(builder).build()
        }
    }

    fun soundPool(id: String, soundPath: String, builder: SoundPoolBuilder.() -> Unit = {}) {
        if (soundPools.containsKey(id)) {
            throw Exception("Sound pool with id $id already exists")
        }
        soundPools[id] = {
            SoundPoolBuilder(id, soundPath).apply(builder).build()
        }
    }

    fun sequence(id: String, builder: SequenceBuilder.() -> Unit) {
        if (sequences.containsKey(id)) {
            throw Exception("Sequence with id $id already exists")
        }
        sequences[id] = {
            SequenceBuilder().apply(builder).build()
        }
    }

    fun onCollision(block: (scene: Scene, result: KotlinCollisionResult) -> Unit) {
        onCollision = block
    }

    @OptIn(ExperimentalForeignApi::class)
    fun build(): SceneInfo {
        //
        // Create a physics world for the scene
        //
        val physicsHandle = memScoped {
            val info = cValue<CreatePhysicsInfo> {
                gravityX = this@SceneBuilder.gravityX
                gravityY = this@SceneBuilder.gravityY
                gravityZ = this@SceneBuilder.gravityZ
            }

            c_createPhysics!!.invoke(info.ptr)
        }

        val scene = SceneImpl(physicsHandle)
        scene.data = data.invoke()

        components.forEach { (id, builder) ->
            scene.components[id] = builder.invoke(scene).apply { onBuildScene.invoke(this@SceneBuilder) }
        }

        spriteSheets.forEach { (id, builder) ->
            scene.spriteSheets[id] = builder.invoke(scene)
        }

        textBatches.forEach { (id, builder) ->
            scene.textBatches[id] = builder.invoke(scene)
        }

        spriteBatches.forEach { (id, builder) ->
            scene.spriteBatches[id] = builder.invoke(scene)
        }

        lights.forEach { (id, builder) ->
            scene.lights[id] = builder.invoke(scene)
        }
        cameras.forEach { (id, builder) ->
            scene.cameras[id] = builder.invoke(scene)
        }
        entities.forEach { (id, builder) ->
            scene.entities[id] = builder.invoke(scene)
        }
        entityPools.forEach { (id, builder) ->
            scene.entityPools[id] = builder.invoke(scene)
        }
        emitters.forEach { (id, builder) ->
            scene.emitters[id] = builder.invoke(scene)
        }
        emitterPools.forEach { (id, builder) ->
            scene.emitterPools[id] = builder.invoke(scene)
        }

        soundPools.forEach { (id, builder) ->
            scene.soundPools[id] = builder.invoke(scene)
        }

        sequences.forEach { (id, builder) ->
            scene.sequences[id] = builder
        }

        return SceneInfo(
            scene,
            onCollision,
            onSceneCreated,
            onSceneUpdate,
            onSceneBeforeUpdate,
            onSceneAfterUpdate,
        )
    }

    fun onSceneCreated(block: (Scene) -> Unit) {
        onSceneCreated = block
    }

    fun onSceneUpdate(block: OnSceneUpdate) {
        onSceneUpdate = block
    }

    fun onBeforeSceneUpdate(block: OnSceneBeforeUpdate) {
        onSceneBeforeUpdate = block
    }

    fun onAfterSceneUpdate(block: OnSceneAfterUpdate) {
        onSceneAfterUpdate = block
    }
}

