package com.fluxtah.application.api

import com.fluxtah.application.api.interop.c_loadSound
import com.fluxtah.application.api.interop.model.CreateSoundInfo
import com.fluxtah.application.api.scene.SceneDsl
import com.fluxtah.application.api.scene.SoundPoolInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped

@OptIn(ExperimentalForeignApi::class)
@SceneDsl
class SoundPoolBuilder(private val id: String, private val soundPath: String) {
    private var loop: Boolean = false

    private var initialSize: Int = 10

    fun initialSize(size: Int) {
        initialSize = size
    }

    fun loop(enabled: Boolean) {
        loop = enabled
    }

    fun build(): SoundPoolInfo {
        val initialSounds = mutableListOf<Sound>().apply {
            repeat(initialSize) {
                add(createSound(soundPath))
            }
        }

        return SoundPoolInfo(
            initialSize = initialSize,
            factory = { createSound(soundPath) },
            sounds = initialSounds,
            soundsAvailable = initialSounds.toMutableList(),
            soundsInUse = mutableListOf()
        )
    }

    private fun createSound(soundPath: String): Sound {
        val info = cValue<CreateSoundInfo> {
            loop = if (this@SoundPoolBuilder.loop) 1 else 0
        }
        val cSound = memScoped { c_loadSound!!.invoke(soundPath.cstr.ptr, info.ptr) }
        return Sound(id, cSound)
    }
}