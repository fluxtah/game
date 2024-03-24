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

class ShipMovementBehavior(
    val forwardAcceleration: Float = 40.0f,
    private val reversingFactor: Float = 2.0f,
    val maxForwardSpeed: Float = 100.0f,
    val maxReverseSpeed: Float = -60.0f,
    private val lateralAcceleration: Float = 40.0f,
    private val maxLateralSpeed: Float = 100.0f,
    private val maxLeanAngle: Float = (10.0f).toRadians()
) : EntityBehavior() {
    val data: ShipData by lazy { entity.data() }

    private lateinit var engineSound: Sound
    private lateinit var movementSound: Sound

    override fun initialize() {
        engineSound = scene.soundFromPool(Id.SOUND_ENGINE)
        engineSound.playIfNotPlaying()

        movementSound = scene.soundFromPool(Id.SOUND_LATERAL_THRUST)
    }

    override fun reset() {
        if (!::engineSound.isInitialized) return
        engineSound.stopIfPlaying()
        entity.setVelocity(z = 0.0f)

        if (!::movementSound.isInitialized) return
        movementSound.stopIfPlaying()
        entity.setVelocity(x = 0.0f)

        // UNDONE: Bullet physics
        //entity.setRotation(z = 0.0f) // Reset lean angle when resetting behavior
    }

    override fun update(time: Float) {

        val velocityX = calculateVelocityX()
        val velocityZ = calculateVelocityZ()
        val forward = entity.getOrientation().getLocalForwardAxis()
        val right = entity.getOrientation().getLocalRightAxis()

        val velocity = forward * velocityZ + right * velocityX

        // Calculate new position based on rotated velocities
        val newPosition = Vector3(entity.positionX, entity.positionY, entity.positionZ) + velocity * fixedTimeStep

        entity.setPosition(newPosition.x, newPosition.y, newPosition.z)
        entity.setVelocity(x = velocityX, z = velocityZ)
        // Apply lean effect based on lateral velocity
        applyLeanEffect()

        engineSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)
        movementSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)
    }

    private fun calculateVelocityX() = when {
        data.input.isMovingRight -> {
            movementSound.playIfNotPlaying()
            (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtMost(maxLateralSpeed)
        }

        data.input.isMovingLeft -> {
            movementSound.playIfNotPlaying()
            (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtLeast(-maxLateralSpeed)
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

    private fun calculateVelocityZ() = when {
        data.input.isMovingForward -> {
            // Increase forward velocity
            (entity.velocityZ + forwardAcceleration * fixedTimeStep).coerceAtMost(maxForwardSpeed)
        }

        data.input.isReversing -> {
            // Decrease forward velocity for reverse movement
            (entity.velocityZ - (forwardAcceleration * reversingFactor) * fixedTimeStep).coerceAtLeast(maxReverseSpeed)
        }

        else -> {
            // Slow down to a halt if not moving forward or reversing
            if (entity.velocityZ > 0) {
                (entity.velocityZ - forwardAcceleration * fixedTimeStep).coerceAtLeast(0.0f)
            } else {
                (entity.velocityZ + forwardAcceleration * fixedTimeStep).coerceAtMost(0.0f)
            }
        }
    }

    fun lerp(a: Float, b: Float, f: Float): Float {
        return a + f * (b - a)
    }

    override fun afterUpdate(time: Float, deltaTime: Float) {
        engineSound.setPitch(abs(0.7f + (entity.velocityZ / maxForwardSpeed) * 0.6f))
    }

    fun startEngine() {
        engineSound.playIfNotPlaying()
    }

    fun stopEngine() {
        engineSound.stopIfPlaying()
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
        // UNDONE: Bullet physics
        //entity.setRotation(z = leanAngle)
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
}


