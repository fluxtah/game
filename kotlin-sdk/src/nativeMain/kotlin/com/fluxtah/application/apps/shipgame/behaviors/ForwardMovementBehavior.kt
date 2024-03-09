package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class ForwardMovementBehavior(
    val acceleration: Float = 20.0f,
    private val reversingFactor: Float = 2.0f,
    val maxForwardSpeed: Float = 100.0f,
    val maxReverseSpeed: Float = -60.0f,
) : EntityBehavior() {
    val data: ShipData by lazy { entity.data() }

    private lateinit var engineSound: Sound

    override fun initialize() {
        engineSound = scene.soundFromPool(Id.SOUND_ENGINE)
        engineSound.playIfNotPlaying()
    }

    override fun reset() {
        if (!::engineSound.isInitialized) return
        engineSound.stopIfPlaying()
        data.velocity.z = 0.0f
    }

    override fun update(time: Float) {

        data.velocity.z = when {
            data.input.isMovingForward -> {
                // Increase forward velocity
                (data.velocity.z + acceleration * fixedTimeStep).coerceAtMost(maxForwardSpeed)
            }

            data.input.isReversing -> {
                // Decrease forward velocity for reverse movement
                (data.velocity.z - (acceleration * reversingFactor) * fixedTimeStep).coerceAtLeast(maxReverseSpeed)
            }

            else -> {
                // Slow down to a halt if not moving forward or reversing
                if (data.velocity.z > 0) {
                    (data.velocity.z - acceleration * fixedTimeStep).coerceAtLeast(0.0f)
                } else {
                    (data.velocity.z + acceleration * fixedTimeStep).coerceAtMost(0.0f)
                }
            }
        }

        // Calculate new position based on forward velocity
        val newPosition = Vector3(entity.positionX, entity.positionY, entity.positionZ) + calculateForwardMovement(
            entity.rotationY,
            data.velocity.z * fixedTimeStep
        )

        entity.setPosition(newPosition.x, newPosition.y, newPosition.z)
        engineSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)
    }

    fun lerp(a: Float, b: Float, f: Float): Float {
        return a + f * (b - a)
    }

    override fun afterUpdate(time: Float, deltaTime: Float) {
        engineSound.setPitch(abs(0.7f + (data.velocity.z / maxForwardSpeed) * 0.6f))
    }

    fun startEngine() {
        engineSound.playIfNotPlaying()
    }

    fun stopEngine() {
        engineSound.stopIfPlaying()
    }
}

fun calculateForwardMovement(yaw: Float, distance: Float): Vector3 {
    // Convert yaw to radians
    val yawRadians = yaw.toRadians()
    val sinYaw = sin(yawRadians)
    val cosYaw = cos(yawRadians)

    // Calculate forward movement based on yaw
    return Vector3(
        distance * sinYaw,
        0f, // Assuming no vertical movement in forward thrust
        distance * cosYaw
    )
}
