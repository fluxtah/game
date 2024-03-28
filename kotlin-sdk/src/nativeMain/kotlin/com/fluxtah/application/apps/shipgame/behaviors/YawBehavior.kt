package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.math.Quaternion
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class YawBehavior : EntityBehavior() {

    private val shipData by lazy { entity.data<ShipData>() }

    private val yawAcceleration = 256.0f // Increased for faster rotation
    private val yawMaxVelocity = 128.0f
    private val dampingFactor = 0.95f // Adjust this value to control the deceleration rate

    private var yaw = 0.0f
    private var yawVelocity = 0.0f

    override fun update(time: Float) {
        if (shipData.input.isYawingLeft) {
            yawVelocity += fixedTimeStep * yawAcceleration
        } else if (shipData.input.isYawingRight) {
            yawVelocity -= fixedTimeStep * yawAcceleration
        } else {
            // Apply stronger damping when not yawing left or right
            yawVelocity *= dampingFactor
        }

        // Ensure yawVelocity stays within its bounds
        yawVelocity = yawVelocity.coerceIn(-yawMaxVelocity, yawMaxVelocity)

        // Update yaw based on the current velocity
        yaw += yawVelocity * fixedTimeStep

        // Calculate and set the new orientation
        val orientation = Quaternion.identity()
        orientation.rotateAroundAxis(Vector3.up, yaw)
        entity.setOrientation(orientation)
    }
}
