package com.fluxtah.application.api.ai

class BooleanAlternator(private val timeFalse: Float, private val timeTrue: Float) {
    private var accumulatedTime = 0.0f
    private var currentState = false // Tracks the current state between true and false

    fun update(deltaTime: Float, onStateChange: (Boolean) -> Unit) {
        accumulatedTime += deltaTime

        if (currentState) {
            // Check if the true state interval has elapsed
            if (accumulatedTime >= timeTrue) {
                onStateChange(false) // Change to false state
                accumulatedTime -= timeTrue // Reset accumulated time for false state interval
                currentState = false // Update current state to false
            }
        } else {
            // Check if the false state interval has elapsed
            if (accumulatedTime >= timeFalse) {
                onStateChange(true) // Change to true state
                accumulatedTime -= timeFalse // Reset accumulated time for true state interval
                currentState = true // Update current state to true
            }
        }
    }
}


