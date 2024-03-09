package com.fluxtah.application.api.ai

class Timer(private val duration: Float) {
    private var accumulatedTime = 0.0f
    private var running = false

    fun update(deltaTime: Float, onElapsed: () -> Unit) {
        if (!running) return
        accumulatedTime += deltaTime

        if (accumulatedTime >= duration) {
            onElapsed()
            accumulatedTime = 0.0f
            running = false
        }
    }

    fun start() {
        accumulatedTime = 0.0f
        running = true
    }

    fun stop() {
        accumulatedTime = 0.0f
        running = false
    }
}