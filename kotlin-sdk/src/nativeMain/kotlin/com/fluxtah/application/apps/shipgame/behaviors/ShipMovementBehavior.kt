package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.math.Quaternion
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class ShipMovementBehavior(
    private val forwardAcceleration: Float = 40.0f,
    private val reversingFactor: Float = 2.0f,
    private val maxForwardSpeed: Float = 100.0f,
    private val maxReverseSpeed: Float = -60.0f,
    private val lateralAcceleration: Float = 40.0f,
    private val maxLateralSpeed: Float = 100.0f,
    private val maxLeanAngle: Float = (7.0f).toRadians(),
    private val yawAcceleration: Float = 256.0f,// Increased for faster rotation
    private val yawMaxVelocity: Float = 100.0f,
    private val yawDampingFactor: Float = 0.96f, // Adjust this value to control the deceleration rate
    private var yaw: Float = 0.0f,
    private var yawVelocity: Float = 0.0f,
    private val dampingFactor: Float = 0.99f // Adjust this value to control the deceleration rate
) : EntityBehavior() {
    val data: ShipData by lazy { entity.data() }

    private var specialManeuver: SpecialManeuver = SpecialManeuver.None

    private lateinit var engineSound: Sound
    private lateinit var movementSound: Sound

    private val boostFactor = 4.0f
    private var boostTimer = 0f
    private val boostDuration = 1.5f // Seconds
    private var postBoostVelocity = 0.0f

    override fun initialize() {
        engineSound = scene.soundFromPool(Id.SOUND_ENGINE)
        engineSound.playIfNotPlaying()

        movementSound = scene.soundFromPool(Id.SOUND_LATERAL_THRUST)
        specialManeuver = SpecialManeuver.None
    }

    override fun reset() {
        if (!::engineSound.isInitialized) return
        engineSound.stopIfPlaying()
        entity.setVelocity(z = 0.0f)

        if (!::movementSound.isInitialized) return
        movementSound.stopIfPlaying()
        entity.setVelocity(x = 0.0f)

        specialManeuver = SpecialManeuver.None
        yaw = 0.0f
        boostTimer = 0f
        postBoostVelocity = 0.0f
    }

    override fun update(time: Float) {
        if (data.input.isBoosting && specialManeuver != SpecialManeuver.Boost) {
            if (data.energy > 10) {
                data.depleteEnergy(10f)
                specialManeuver = SpecialManeuver.Boost
            }
        }

        when (specialManeuver) {
            SpecialManeuver.None -> stepMovement()
            SpecialManeuver.BarrelRoll -> stepMovement()
            SpecialManeuver.Boost -> boost()
        }
    }

    private fun boost() {

        // Only boost for a limited duration
        if (boostTimer < boostDuration) {
            boostTimer += fixedTimeStep
        } else {
            specialManeuver = SpecialManeuver.None
            boostTimer = 0f
            postBoostVelocity = (maxForwardSpeed * boostFactor) - entity.velocityZ
            return
        }

        // Increase forward velocity
        val velocityZ =
            (entity.velocityZ + (forwardAcceleration * boostFactor) * fixedTimeStep).coerceAtMost(maxForwardSpeed * boostFactor)

        // Smooth down lateral velocity
        if (entity.velocityX > 0) {
            (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtLeast(0.0f) * dampingFactor
        } else {
            (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtMost(0.0f) * dampingFactor
        }

        val forward = entity.getOrientation().getLocalForwardAxis()
        val velocity = forward * velocityZ

        val newPosition = Vector3(entity.positionX, entity.positionY, entity.positionZ) + velocity * fixedTimeStep

        entity.setPosition(newPosition.x, newPosition.y, newPosition.z)
        entity.setVelocity(z = velocityZ)
    }

    private fun stepMovement() {
        val velocityX = calculateVelocityX()
        val velocityZ = calculateVelocityZ()

        val forward = entity.getOrientation().getLocalForwardAxis()
        val right = entity.getOrientation().getLocalRightAxis()

        val velocity = forward * velocityZ + right * velocityX

        // Calculate new position based on rotated velocities
        val newPosition = Vector3(entity.positionX, entity.positionY, entity.positionZ) + velocity * fixedTimeStep

        entity.setPosition(newPosition.x, newPosition.y, newPosition.z)
        entity.setVelocity(x = velocityX, z = velocityZ)

        engineSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)
        movementSound.setSoundPosition(newPosition.x, newPosition.y, newPosition.z)

        if (data.input.isYawingLeft) {
            yawVelocity += fixedTimeStep * yawAcceleration
        } else if (data.input.isYawingRight) {
            yawVelocity -= fixedTimeStep * yawAcceleration
        } else {
            // Apply stronger damping when not yawing left or right
            yawVelocity *= yawDampingFactor
        }

        // Ensure yawVelocity stays within its bounds
        yawVelocity = yawVelocity.coerceIn(-yawMaxVelocity, yawMaxVelocity)

        // Update yaw based on the current velocity
        yaw += yawVelocity * fixedTimeStep

        // Calculate and set the new orientation
        val orientation = Quaternion.identity()
        orientation.rotateAroundAxis(Vector3.up, yaw)
        entity.setOrientation(orientation)

        // Apply lean effect based on lateral velocity
        applyLeanEffect()
    }

    private fun calculateVelocityX() = when {
        data.input.isMovingRight -> {
            movementSound.playIfNotPlaying()
            (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtLeast(-maxLateralSpeed)
        }

        data.input.isMovingLeft -> {
            movementSound.playIfNotPlaying()
            (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtMost(maxLateralSpeed)
        }

        else -> {
            movementSound.stopIfPlaying()
            if (entity.velocityX > 0) {
                (entity.velocityX - lateralAcceleration * fixedTimeStep).coerceAtLeast(0.0f) * dampingFactor
            } else {
                (entity.velocityX + lateralAcceleration * fixedTimeStep).coerceAtMost(0.0f) * dampingFactor
            }
        }
    }

    private fun calculateVelocityZ(): Float {
        postBoostVelocity *= dampingFactor
        return when {
            data.input.isMovingForward -> {
                // Increase forward velocity
                (entity.velocityZ + forwardAcceleration * fixedTimeStep).coerceAtMost(maxForwardSpeed + postBoostVelocity)
            }

            data.input.isReversing -> {
                // Decrease forward velocity for reverse movement
                (entity.velocityZ - (forwardAcceleration * reversingFactor) * fixedTimeStep).coerceAtLeast(
                    maxReverseSpeed
                )
            }

            else -> {
                // Slow down to a halt if not moving forward or reversing
                if (entity.velocityZ > 0) {
                    (entity.velocityZ - forwardAcceleration * fixedTimeStep).coerceAtLeast(0.0f) * dampingFactor
                } else {
                    (entity.velocityZ + forwardAcceleration * fixedTimeStep).coerceAtMost(0.0f) * dampingFactor
                }
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

    private fun applyLeanEffect() {
        val leanAngle = -((entity.velocityX / maxLateralSpeed * maxLeanAngle).toDouble())

        // Get the entity's current orientation to preserve it
        val currentOrientation = entity.getOrientation()

        // Calculate the quaternion for the desired lean angle around the Z axis
        val halfAngle = leanAngle / 2
        val sinHalfAngle = sin(halfAngle).toFloat()
        val cosHalfAngle = cos(halfAngle).toFloat()

        // Quaternion representing rotation around the Z-axis by the lean angle
        val leanQuaternion = Quaternion(cosHalfAngle, 0f, 0f, sinHalfAngle)

        // Combine the current orientation with the lean quaternion
        // Assuming that Quaternion multiplication (*) is defined to combine rotations
        val combinedOrientation = currentOrientation * leanQuaternion

        entity.setOrientation(combinedOrientation)
    }
}

enum class SpecialManeuver {
    None,
    BarrelRoll,
    Boost
}


