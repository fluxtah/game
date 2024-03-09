package com.fluxtah.application.api

import com.fluxtah.application.api.interop.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class Sound(
    val id: String,
    val handle: CSound,
    var inUse: Boolean = false
) {

    fun play() {
        c_playSound!!.invoke(handle)
    }

    fun playIfNotPlaying() {
        if (!isPlaying()) {
            play()
        }
    }

    fun isPlaying(): Boolean {
        return c_isSoundPlaying!!.invoke(handle) == 1
    }

    fun stop() {
        c_stopSound!!.invoke(handle)
    }

    fun stopIfPlaying() {
        if (isPlaying()) {
            stop()
        }
    }

    fun setPitch(pitch: Float) {
        c_setSoundPitch!!.invoke(handle, pitch)
    }

    fun setSoundPosition(x: Float, y: Float, z: Float) {
        c_setSoundPosition!!.invoke(handle, x, y, z)
    }
}

