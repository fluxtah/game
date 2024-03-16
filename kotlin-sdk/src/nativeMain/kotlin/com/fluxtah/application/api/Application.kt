package com.fluxtah.application.api

import com.fluxtah.application.api.input.Key
import com.fluxtah.application.api.interop.c_destroyCamera
import com.fluxtah.application.api.interop.c_destroyEmitter
import com.fluxtah.application.api.interop.c_destroyEntity
import com.fluxtah.application.api.interop.c_destroyLight
import com.fluxtah.application.api.interop.c_destroyPhysics
import com.fluxtah.application.api.interop.c_destroySound
import com.fluxtah.application.api.interop.c_destroySpriteBatch
import com.fluxtah.application.api.interop.c_destroySpriteSheet
import com.fluxtah.application.api.interop.c_destroyTextBatch
import com.fluxtah.application.api.interop.c_isKeyPressed
import com.fluxtah.application.api.interop.c_removeEntityPhysics
import com.fluxtah.application.api.interop.c_setEnableDebugBoundingVolumes
import com.fluxtah.application.api.interop.c_stepPhysicsSimulation
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.SceneImpl
import com.fluxtah.application.api.scene.activeSceneInfo
import com.fluxtah.application.apps.shipgame.ShipGame
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi

interface Application {
    fun initialize()
    fun beforeUpdate(time: Float, deltaTime: Float) {}
    fun update(time: Float) {}
    fun afterUpdate(time: Float, deltaTime: Float) {}
}

fun isKeyPressed(key: Key): Boolean {
    return c_isKeyPressed?.invoke(key.value) == 1
}

@OptIn(ExperimentalForeignApi::class)
fun enableDebugBoundingVolumes(enable: Boolean) {
    c_setEnableDebugBoundingVolumes?.invoke(ApplicationContext.vulcanContext!!, enable)
}

fun serverMode(): ServerMode {
    // TODO
    return ServerMode.Client
}

enum class ServerMode {
    Headless,
    Host,
    Client
}

private lateinit var applicationInstance: Application

@OptIn(ExperimentalNativeApi::class)
@CName("ktCreateApplication")
fun ktCreateApplication() {
    applicationInstance = ShipGame()
}

@OptIn(ExperimentalNativeApi::class)
@CName("ktInitApplication")
fun ktInitApplication() {
    applicationInstance.initialize()
}

const val fixedTimeStep = 1.0f / 60.0f // Fixed timestep (e.g., 60 updates per second)
private var accumulatedTime = 0.0f

private var screenWidth = 0
private var screenHeight = 0

fun getScreenWidth(): Int {
    return screenWidth
}

fun getScreenHeight(): Int {
    return screenHeight
}

@OptIn(ExperimentalNativeApi::class)
@CName("ktSetScreenSize")
fun ktSetScreenSize(width: Int, height: Int) {
    screenWidth = width
    screenHeight = height
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("ktStepPhysics")
fun ktStepPhysics() {
    val activeSceneInfo = activeSceneInfo
    if (activeSceneInfo.scene is Scene.EMPTY) {
        return
    }
    val scene = activeSceneInfo.scene
    if(scene is SceneImpl) {
        c_stepPhysicsSimulation?.invoke(scene.physicsHandle, fixedTimeStep)
    }
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("ktUpdateApplication")
fun ktUpdateApplication(time: Float, deltaTime: Float) {
    accumulatedTime += deltaTime
    val activeSceneInfo = activeSceneInfo
    if (activeSceneInfo.scene is Scene.EMPTY) {
        return
    }
    val scene = activeSceneInfo.scene
    val entities = scene.entities.filter { it.value.entity.active }
        .map { it.value } + scene.entityPools.flatMap { it.value.entityPool.entitiesInUse }.filter { it.entity.active }
    val emitters = scene.emitters.map { it.value } + scene.emitterPools.flatMap { it.value.emittersInUse }
    val cameras = scene.cameras

    applicationInstance.beforeUpdate(time, deltaTime)

    scene.sequencesPlaying.toList().forEach { sequence ->
        sequence.advance(time, deltaTime)
    }

    activeSceneInfo.onSceneBeforeUpdate?.invoke(activeSceneInfo.scene, time, deltaTime)

    scene.components.forEach {
        it.value.onBeforeSceneUpdate(time, deltaTime)
    }

    entities.forEach {
        it.behaviors.forEach { behavior ->
            behavior.beforeUpdate(time, deltaTime)
        }
        it.onSceneBeforeEntityUpdate?.invoke(scene, it.entity, time, deltaTime)
    }
    emitters.forEach {
        it.behaviors.forEach { behavior ->
            behavior.beforeUpdate(time, deltaTime)
        }
    }
    cameras.forEach {
        it.value.behaviors.forEach { behavior ->
            behavior.beforeUpdate(time, deltaTime)
        }
    }

    while (accumulatedTime >= fixedTimeStep) {
        activeSceneInfo.onSceneUpdate?.invoke(activeSceneInfo.scene, time)
        scene.components.forEach {
            it.value.onSceneUpdate(time)
        }
        entities.forEach {
            it.behaviors.forEach { behavior ->
                behavior.update(time)
            }
            it.onSceneEntityUpdate?.invoke(scene, it.entity, time)
        }
        emitters.forEach {
            it.behaviors.forEach { behavior ->
                behavior.update(time)
            }
        }
        cameras.forEach {
            it.value.behaviors.forEach { behavior ->
                behavior.update(time)
            }
        }
        applicationInstance.update(time)
        accumulatedTime -= fixedTimeStep
    }


    activeSceneInfo.onSceneAfterUpdate?.invoke(activeSceneInfo.scene, time, deltaTime)

    scene.components.forEach {
        it.value.onAfterSceneUpdate(time, deltaTime)
    }

    entities.forEach {
        it.behaviors.forEach { behavior ->
            behavior.afterUpdate(time, deltaTime)
        }
        it.onSceneAfterEntityUpdate?.invoke(scene, it.entity, time, deltaTime)
    }
    emitters.forEach {
        it.behaviors.forEach { behavior ->
            behavior.afterUpdate(time, deltaTime)
        }
    }
    cameras.forEach {
        it.value.behaviors.forEach { behavior ->
            behavior.afterUpdate(time, deltaTime)
        }
    }

    applicationInstance.afterUpdate(time, deltaTime)
}

@OptIn(ExperimentalNativeApi::class)
@CName("ktDestroyApplication")
fun ktDestroyApplication() {
    // TODO: Destroy all scenes
    activeSceneInfo.scene.destroy()
}

@OptIn(ExperimentalForeignApi::class)
private fun BaseScene.destroy() {
    textBatches.forEach { textBatch ->
        c_destroyTextBatch!!.invoke(ApplicationContext.vulcanContext!!, textBatch.value.handle)
    }
    textBatches.clear()

    spriteBatches.forEach { spriteBatch ->
        c_destroySpriteBatch!!.invoke(ApplicationContext.vulcanContext!!, spriteBatch.value.handle)
    }
    spriteBatches.clear()

    spriteSheets.forEach { spriteSheet ->
        c_destroySpriteSheet!!.invoke(ApplicationContext.vulcanContext!!, spriteSheet.value.handle)
    }
    spriteSheets.clear()

    lights.forEach { light ->
        c_destroyLight!!.invoke(light.value.handle)
    }
    lights.clear()

    cameras.forEach { camera ->
        c_destroyCamera!!.invoke(camera.value.handle)
    }
    cameras.clear()

    entities.forEach { entityInfo ->
        if (entityInfo.value.entity.physicsEnabled) {
            c_removeEntityPhysics!!.invoke(entityInfo.value.entity.handle, (this as SceneImpl).physicsHandle)
        }
        c_destroyEntity!!.invoke(ApplicationContext.vulcanContext!!, entityInfo.value.entity.handle)
        entityInfo.value.stableRef!!.dispose()
    }
    entities.clear()

    entityPools.values.forEach { entityPoolInfo ->
        entityPoolInfo.entityPool.entities.forEach { entityInfo ->
            if (entityInfo.entity.physicsEnabled) {
                c_removeEntityPhysics!!.invoke(entityInfo.entity.handle, (this as SceneImpl).physicsHandle)
            }

            c_destroyEntity!!.invoke(ApplicationContext.vulcanContext!!, entityInfo.entity.handle)
            entityInfo.stableRef!!.dispose()
        }
        entityPoolInfo.entityPool.entitiesInUse.clear()
        entityPoolInfo.entityPool.entitiesAvailable.clear()
    }

    emitters.forEach { emitterInfo ->
        c_destroyEmitter!!.invoke(ApplicationContext.vulcanContext!!, emitterInfo.value.emitter.handle)
    }
    emitters.clear()
    emitterPools.forEach { emitterPool ->
        emitterPool.value.emittersInUse.forEach { emitterInfo ->
            c_destroyEmitter!!.invoke(ApplicationContext.vulcanContext!!, emitterInfo.emitter.handle)
        }
        emitterPool.value.emittersInUse.clear()
        emitterPool.value.emittersAvailable.forEach { emitterInfo ->
            c_destroyEmitter!!.invoke(ApplicationContext.vulcanContext!!, emitterInfo.emitter.handle)
        }
        emitterPool.value.emittersAvailable.clear()
    }

    soundPools.forEach { soundPool ->
        soundPool.value.sounds.forEach {
            c_destroySound!!.invoke(it.handle)
        }
    }
    soundPools.clear()

    if (this is SceneImpl) {
        c_destroyPhysics!!.invoke(physicsHandle)
    }
}
