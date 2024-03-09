package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class ShipShieldBehaviour : EntityBehavior() {
    private val shipData by lazy { entity.data<ShipData>() }

    private val replenishRate = 0.1f
    private var lastReplenishedTime = 0.0f
    private var replenishAmount = 1f

    override fun beforeUpdate(time: Float, deltaTime: Float) {
        if (!entity.active) {
            return
        }
        if (time - lastReplenishedTime > replenishRate) {
            shipData.replenishShield(replenishAmount)
            lastReplenishedTime = time
        }
    }
}