package com.fluxtah.application.api.scene

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.Light
import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.camera.Camera
import com.fluxtah.application.api.emitter.Emitter
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.interop.c_setActiveCamera
import com.fluxtah.application.api.sequence.Sequence
import com.fluxtah.application.api.sprite.SpriteBatch
import com.fluxtah.application.api.sprite.SpriteSheet
import com.fluxtah.application.api.text.TextBatch
import kotlinx.cinterop.ExperimentalForeignApi


abstract class BaseScene : Scene {
    lateinit var data: Any
    var activeCamera: Camera? = null
    val components = mutableMapOf<String, SceneComponent>()
    val spriteSheets = mutableMapOf<String, SpriteSheet>()
    val textBatches = mutableMapOf<String, TextBatch>()
    val spriteBatches = mutableMapOf<String, SpriteBatch>()
    val cameras = mutableMapOf<String, Camera>()
    val lights = mutableMapOf<String, Light>()
    val entities = mutableMapOf<String, EntityInfo>()
    val entityPools = mutableMapOf<String, EntityPoolInfo>()
    val emitters = mutableMapOf<String, EmitterInfo>()
    val emitterPools = mutableMapOf<String, EmitterPoolInfo>()
    val soundPools = mutableMapOf<String, SoundPoolInfo>()
    val sequences = mutableMapOf<String, () -> Sequence>()
    val sequencesPlaying = mutableListOf<Sequence>()

    fun dispatchSequence(sequence: Sequence) {
        sequencesPlaying.add(sequence)
    }

    fun stopSequence(sequence: Sequence) {
        sequencesPlaying.remove(sequence)
    }
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalForeignApi::class)
class SceneImpl : BaseScene() {
    override fun setActiveCamera(id: String) {
        activeCamera = cameras[id] ?: throw Exception("Camera with id $id does not exist")
        c_setActiveCamera!!.invoke(ApplicationContext.vulcanContext!!, activeCamera!!.handle)
    }

    override fun activeCamera(): Camera? {
        return activeCamera
    }

    override fun cameraById(id: String): Camera? {
        return cameras[id]
    }

    override fun entityById(id: String): Entity {
        val entity = entities[id] ?: throw Exception("Entity with id $id does not exist")
        return entity.entity
    }

    override fun entityInPoolByCondition(poolId: String, condition: (entity: Entity) -> Boolean): Entity? {
        val info = entityPools[poolId] ?: throw Exception("Entity pool with id $poolId does not exist")
        return info.entityPool.entities.firstOrNull { condition(it.entity) }?.entity
    }

    override fun emitterById(id: String): Emitter? {
        return emitters[id]?.emitter
    }

    override fun entityFromPool(id: String, block: (entity: Entity) -> Unit) {
        val info = entityPools[id] ?: throw Exception("Entity pool with id $id does not exist")

        val pool = info.entityPool
        if (pool.entitiesAvailable.any()) {
            val entity = pool.entitiesAvailable.removeAt(0)
            block(entity.entity)
            entity.behaviors.forEach { behavior -> behavior.initialize() }
            pool.entitiesInUse.add(entity)
            entity.entity.active = true
        } else {
            println("No entities available in pool: $id\n")
        }
    }

    override fun entityToPool(entity: Entity) {
        val info = entityPools[entity.id] ?: throw Exception("Entity with ${entity.id} is not from a pool")
        val pool = info.entityPool
        val entityInfo =
            pool.entitiesInUse.find { it.entity == entity }
                ?: throw Exception("Entity with ${entity.id} is not in use, check inUse before adding it back to the pool")
        pool.entitiesInUse.remove(entityInfo)
        entity.active = false
        pool.entitiesAvailable.add(entityInfo)
    }

    override fun resetEntityPool(id: String) {
        val info = entityPools[id] ?: throw Exception("Entity pool with id $id does not exist")
        val pool = info.entityPool
        pool.entitiesInUse.forEach {
            it.entity.active = false
            pool.entitiesAvailable.add(it)
        }
        pool.entitiesInUse.clear()
    }

    override fun emitterFromPool(id: String, block: (emitter: Emitter) -> Unit) {
        val pool = emitterPools[id] ?: throw Exception("Emitter pool with id $id does not exist")

        if (pool.emittersAvailable.any()) {
            val emitter = pool.emittersAvailable.removeAt(0)
            block(emitter.emitter)
            emitter.behaviors.forEach { behavior -> behavior.initialize() }
            pool.emittersInUse.add(emitter)
            emitter.emitter.inUse = true
        }
    }

    override fun emitterToPool(emitter: Emitter) {
        val pool = emitterPools[emitter.id] ?: throw Exception("Entity with ${emitter.id} is not from a pool")
        val emitterInfo =
            pool.emittersInUse.find { it.emitter == emitter }
                ?: throw Exception("Entity with ${emitter.id} is not in use, check inUse before adding it back to the pool")
        pool.emittersInUse.remove(emitterInfo)
        emitter.inUse = false
        pool.emittersAvailable.add(emitterInfo)
    }

    override fun resetEmitterPool(id: String) {
        val pool = emitterPools[id] ?: throw Exception("Emitter pool with id $id does not exist")
        pool.emittersInUse.forEach {
            it.emitter.inUse = false
            pool.emittersAvailable.add(it)
        }
        pool.emittersInUse.clear()
    }

    override fun soundFromPool(id: String): Sound {
        val pool = soundPools[id] ?: throw Exception("Sound pool with id $id does not exist")

        if (pool.soundsAvailable.any()) {
            val sound = pool.soundsAvailable.removeAt(0)
            pool.soundsInUse.add(sound)
            sound.inUse = true
            return sound
        } else {
            val sound = pool.soundsInUse.removeAt(0)
            sound.stopIfPlaying()
            pool.soundsInUse.add(sound)
            sound.inUse = true
            return sound
        }
    }

    override fun soundToPool(sound: Sound) {
        val pool = soundPools[sound.id] ?: throw Exception("Sound with ${sound.id} is not from a pool")
        val emitterInfo =
            pool.soundsInUse.find { it == sound }
                ?: throw Exception("Sound with ${sound.id} is not in use, check inUse before adding it back to the pool")
        pool.soundsInUse.remove(emitterInfo)
        sound.inUse = false
        pool.soundsAvailable.add(emitterInfo)
    }

    override fun createSequence(id: String): Sequence? {
        val factory = sequences[id]
        if (factory != null) {
            val sequence = factory.invoke()
            sequence.scene = this
            return sequence
        }
        throw Exception("Sequence with id $id does not exist")
    }

    override fun textBatchById(id: String): TextBatch {
        val textBatch = textBatches[id] ?: throw Exception("TextBatch with id $id does not exist")
        return textBatch
    }

    override fun spriteBatchById(id: String): SpriteBatch? {
        return spriteBatches[id]
    }

    override fun <T : SceneComponent> componentById(id: String): T {
        val component = components[id] ?: throw Exception("Component with id $id does not exist")

        return try {
            component as T
        } catch (e: ClassCastException) {
            throw Exception("Component with id $id is not of type: ${e.message}")
        }
    }

    override fun entitiesInPool(id: String): List<Entity> {
        val info = entityPools[id] ?: throw Exception("Entity pool with id $id does not exist")
        return info.entityPool.entities.map { it.entity }
    }

    override fun entityPool(id: String): EntityPool {
        val info = entityPools[id] ?: throw Exception("Entity pool with id $id does not exist")
        return info.entityPool
    }

    override fun <T> data(): T = data as T
}