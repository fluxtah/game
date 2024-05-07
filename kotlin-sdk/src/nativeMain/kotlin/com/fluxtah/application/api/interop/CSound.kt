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
fun ktSetLoadSoundFunc(fn: CPointer<CFunction<LoadSoundFunc>>) {
    c_loadSound = { name, info ->
        memScoped {
            fn.reinterpret<CFunction<LoadSoundFunc>>()(name, info)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroySoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroySound: DestroySoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroySoundFunc")
fun ktSetDestroySoundFunc(fn: CPointer<CFunction<DestroySoundFunc>>) {
    c_destroySound = { sound ->
        memScoped {
            fn.reinterpret<CFunction<DestroySoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias PlaySoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_playSound: PlaySoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPlaySoundFunc")
fun ktSetPlaySoundFunc(fn: CPointer<CFunction<PlaySoundFunc>>) {
    c_playSound = { sound ->
        memScoped {
            fn.reinterpret<CFunction<PlaySoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias IsSoundPlayingFunc = (CSound) -> Int

@OptIn(ExperimentalForeignApi::class)
var c_isSoundPlaying: IsSoundPlayingFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetIsSoundPlayingFunc")
fun ktSetIsSoundPlayingFunc(fn: CPointer<CFunction<IsSoundPlayingFunc>>) {
    c_isSoundPlaying = { sound ->
        memScoped {
            fn.reinterpret<CFunction<IsSoundPlayingFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias StopSoundFunc = (CSound) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_stopSound: StopSoundFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetStopSoundFunc")
fun ktSetStopSoundFunc(fn: CPointer<CFunction<StopSoundFunc>>) {
    c_stopSound = { sound ->
        memScoped {
            fn.reinterpret<CFunction<StopSoundFunc>>()(sound)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetSoundPitchFunc = (CSound, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setSoundPitch: SetSoundPitchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetSoundPitchFunc")
fun ktSetSoundPitchFunc(fn: CPointer<CFunction<SetSoundPitchFunc>>) {
    c_setSoundPitch = { sound, pitch ->
        memScoped {
            fn.reinterpret<CFunction<SetSoundPitchFunc>>()(sound, pitch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetSoundPositionFunc = (CSound, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setSoundPosition: SetSoundPositionFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetSoundPositionFunc")
fun ktSetSoundPositionFunc(fn: CPointer<CFunction<SetSoundPositionFunc>>) {
    c_setSoundPosition = { sound, x, y, z ->
        memScoped {
            fn.reinterpret<CFunction<SetSoundPositionFunc>>()(sound, x, y, z)
        }
    }
}

