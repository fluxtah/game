package com.fluxtah.application.api.ai

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene

@Suppress("UNCHECKED_CAST")
class AiEntityStateMachine<T>(val defaultState: T, val scene: Scene, val entity: Entity) {
    private val schedules = mutableMapOf<T, AiSchedule>()

    private var currentState: T = defaultState
    private var desiredState: T? = null
    private var desiredStateTime: Float = 0.0f

    // Interval in seconds between schedule updates
    private val updateInterval: Float = 0.001f // Set this to your desired interval
    private var timeSinceLastUpdate: Float = 0.0f

    fun addSchedule(key: T, schedule: AiSchedule) {
        schedule.scene = scene
        schedule.entity = entity
        schedule.machine = this
        schedules[key] = schedule
    }

    fun changeState(newState: Any) {
       // println("Changing state to $newState")
        desiredState = null
        schedules[currentState]?.exit()
        currentState = newState as T
        schedules[currentState]?.enter()
    }

    fun changeStateAt(newState: Any, time: Float) {
       // println("Changing state to $newState at $time")
        desiredState = newState as T
        desiredStateTime = time
    }

    fun update(time: Float, deltaTime: Float) {
        if (desiredState != null && time >= desiredStateTime) {
        //    println("Changing state to $desiredState")
            if (currentState != desiredState) {
                schedules[currentState]?.exit()
                schedules[desiredState!!]?.enter()
            }
            currentState = desiredState!!
            desiredState = null
            timeSinceLastUpdate = 0.0f // Reset the timer upon state change
        }

        timeSinceLastUpdate += deltaTime

        if (timeSinceLastUpdate >= updateInterval) {
            schedules[currentState]?.update(time, deltaTime)
            timeSinceLastUpdate = 0.0f // Reset the timer after the schedule updates
        }
    }

    fun resetState() {
        desiredState = null
        schedules[currentState]?.exit()
        currentState = defaultState
        schedules[currentState]?.enter()
    }

    fun clearSchedules() {
        schedules.clear()
    }

    fun resetAndClearSchedules() {
        resetState()
        clearSchedules()
    }
}
