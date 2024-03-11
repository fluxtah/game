package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.cos
import kotlin.math.sin

class LateralMovementBehavior(
    private val lateralAcceleration: Float = 40.0f,
    private val maxLateralSpeed: Float = 100.0f,
    private val maxLeanAngle: Float = 10.0f // Maximum lean angle in degrees
) : EntityBehavior() {

    private val shipData: ShipData by lazy { entity.data() }

    private lateinit var movementSound: Sound

    override fun initialize() {
        movementSound = scene.soundFromPool(Id.SOUND_LATERAL_THRUST)
    }

    override fun reset() {
        if (!::movementSound.isInitialized) return
        movementSound.stopIfPlaying()
        entity.setVelocity(x = 0.0f)
        entity.setRotation(z = 0.0f) // Reset lean angle when resetting behavior
    }

    override fun update(time: Float) {
        val velocityX = when {
            shipData.input.isMovingRight -> {
                movementSound.playIfNotPlaying()
                (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtMost(maxLateralSpeed)
            }

            shipData.input.isMovingLeft -> {
                movementSound.playIfNotPlaying()
                (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtLeast(-maxLateralSpeed)
            }

            else -> {
                movementSound.stopIfPlaying()
                if (entity.velocityX > 0) {
                    (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtLeast(0.0f)
                } else {
                    (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtMost(0.0f)
                }
            }
        }

        entity.setVelocity(x = velocityX)

        val newPosition = Vector3(entity.positionX, entity.positionY, entity.positionZ) + calculateLateralMovement(
            entity.rotationY,
            entity.velocityX * fixedTimeStep
        )

        entity.setPosition(newPosition.x, newPosition.y, newPosition.z)
        movementSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)

        // Apply lean effect based on lateral velocity
        applyLeanEffect()
    }

    private fun calculateLateralMovement(yaw: Float, distance: Float): Vector3 {
        val yawRadians = yaw.toRadians()
        val forwardDirection = Vector3(sin(yawRadians), 0f, cos(yawRadians))
        val leftDirection = Vector3(cos(yawRadians), 0f, -sin(yawRadians))
        val rightDirection = Vector3(-cos(yawRadians), 0f, sin(yawRadians))
        val lateralDirection = if (entity.velocityX > 0) rightDirection else -leftDirection
        return lateralDirection * distance
    }

    private fun applyLeanEffect() {
        // Calculate lean angle based on lateral velocity, ensuring it does not exceed maxLeanAngle
        val leanAngle = (entity.velocityX / maxLateralSpeed) * maxLeanAngle
        // Apply lean angle to entity's Z-axis rotation
        entity.setRotation(z = leanAngle)
    }
}
