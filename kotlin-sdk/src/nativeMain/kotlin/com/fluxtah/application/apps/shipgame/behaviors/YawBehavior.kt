package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class YawBehavior : EntityBehavior() {

    private val shipData by lazy { entity.data<ShipData>() }

    private val yawSpeed = 50.0f // Increased for faster rotation

    override fun update(time: Float) {
        var yawDirection = 0.0f
        if (shipData.input.isYawingLeft) {
            yawDirection += 1.0f
        }
        if (shipData.input.isYawingRight) {
            yawDirection -= 1.0f
        }

        val yawIncrement = yawSpeed * yawDirection * fixedTimeStep

        val orientation = entity.getOrientation()
        orientation.rotateAroundAxis(Vector3.up, yawIncrement)
        entity.setOrientation(orientation)
    }
}
