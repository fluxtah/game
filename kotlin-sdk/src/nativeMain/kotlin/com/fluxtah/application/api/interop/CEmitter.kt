package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateEmitterInfo
import com.fluxtah.application.api.interop.model.EmitterArray
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CEmitter = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateEmitterInfo = CPointer<CreateEmitterInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateEmitterFunc = (CApplicationContext, CCreateEmitterInfo) -> CEmitter

@OptIn(ExperimentalForeignApi::class)
var c_createEmitter: CreateEmitterFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateEmitterFunc")
fun ktSetCreateEmitterFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreateEmitterFunc>>) {
    c_createEmitter = { context, info ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreateEmitterFunc>>()(
                context,
                info
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyEmitterFunc = (CApplicationContext, CEmitter) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyEmitter: DestroyEmitterFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyEmitterFunc")
fun ktSetDestroyEmitterFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CApplicationContext, CEmitter) -> Unit>>) {
    c_destroyEmitter = { device, emitter ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CApplicationContext, CEmitter) -> Unit>>()(device, emitter)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEmitters")
fun ktGetEmitters(): CPointer<EmitterArray> {
    val scene = activeSceneInfo.scene
    val emitters = scene.emitters.values.filter { it.emitter.visible }.map { it.emitter.handle } +
            scene.emitterPools.flatMap {
                it.value.emittersInUse.filter { it.emitter.visible }.map { it.emitter.handle }
            }

    val emitterPointerArray = nativeHeap.allocArray<COpaquePointerVar>(emitters.size)

    emitters.forEachIndexed { index, emitter ->
        emitterPointerArray[index] = emitter
    }

    val emitterArray = nativeHeap.alloc<EmitterArray>()
    emitterArray.emitters = emitterPointerArray
    emitterArray.size = emitters.size

    return emitterArray.ptr
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterPositionFunc = (CEmitter, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEmitterPosition: EmitterPositionFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPositionEmitterFunc")
fun ktSetPositionEmitterFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CEmitter, Float, Float, Float) -> Unit>>) {
    c_setEmitterPosition = { emitter, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CEmitter, Float, Float, Float) -> Unit>>()(
                emitter, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterRotationFunc = (CEmitter, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEmitterRotation: EmitterRotationFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEmitterRotationFunc")
fun ktSetEmitterRotationFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EmitterRotationFunc>>) {
    c_setEmitterRotation = { emitter, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EmitterRotationFunc>>()(
                emitter, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterScaleFunc = (CEmitter, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEmitterScale: EmitterScaleFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEmitterScaleFunc")
fun ktSetEmitterScaleFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EmitterScaleFunc>>) {
    c_setEmitterScale = { emitter, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EmitterScaleFunc>>()(
                emitter, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterResetFunc = (CEmitter) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_resetEmitter: EmitterResetFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEmitterResetFunc")
fun ktSetEmitterResetFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EmitterResetFunc>>) {
    c_resetEmitter = { emitter ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EmitterResetFunc>>()(emitter)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterSpawnRateFunc = (CEmitter, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEmitterSpawnRate: EmitterSpawnRateFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEmitterSpawnRateFunc")
fun ktSetEmitterSpawnRateFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EmitterSpawnRateFunc>>) {
    c_setEmitterSpawnRate = { emitter, rate ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EmitterSpawnRateFunc>>()(
                emitter, rate
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EmitterLifetimeFunc = (CEmitter, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEmitterLifetime: EmitterLifetimeFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEmitterLifetimeFunc")
fun ktSetEmitterLifetimeFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EmitterLifetimeFunc>>) {
    c_setEmitterLifetime = { emitter, lifetime ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EmitterLifetimeFunc>>()(
                emitter, lifetime
            )
        }
    }
}
