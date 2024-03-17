package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateSoundInfo
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CSound = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateSoundInfo = CPointer<CreateSoundInfo>

@OptIn(ExperimentalForeignApi::class)
typealias LoadSoundFunc = (CPointer<ByteVar>, CCreateSoundInfo) -> CSound

@OptIn(ExperimentalForeignApi::class)
var c_loadSound: LoadSoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetLoadSoundFunc")
fun ktSetLoadSoundFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<LoadSoundFunc>>) {
    c_loadSound = { name, info ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<LoadSoundFunc>>()(name, info)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroySoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroySound: DestroySoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroySoundFunc")
fun ktSetDestroySoundFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<DestroySoundFunc>>) {
    c_destroySound = { sound ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<DestroySoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias PlaySoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_playSound: PlaySoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPlaySoundFunc")
fun ktSetPlaySoundFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<PlaySoundFunc>>) {
    c_playSound = { sound ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<PlaySoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias IsSoundPlayingFunc = (CSound) -> Int

@OptIn(ExperimentalForeignApi::class)
var c_isSoundPlaying: IsSoundPlayingFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetIsSoundPlayingFunc")
fun ktSetIsSoundPlayingFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<IsSoundPlayingFunc>>) {
    c_isSoundPlaying = { sound ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<IsSoundPlayingFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias StopSoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_stopSound: StopSoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetStopSoundFunc")
fun ktSetStopSoundFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<StopSoundFunc>>) {
    c_stopSound = { sound ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<StopSoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetSoundPitchFunc = (CSound, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setSoundPitch: SetSoundPitchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetSoundPitchFunc")
fun ktSetSoundPitchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<SetSoundPitchFunc>>) {
    c_setSoundPitch = { sound, pitch ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<SetSoundPitchFunc>>()(sound, pitch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetSoundPositionFunc = (CSound, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setSoundPosition: SetSoundPositionFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetSoundPositionFunc")
fun ktSetSoundPositionFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<SetSoundPositionFunc>>) {
    c_setSoundPosition = { sound, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<SetSoundPositionFunc>>()(sound, x, y, z)
        }
    }
}

