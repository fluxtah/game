package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class ShipEnergyBehaviour : EntityBehavior() {
    private val shipData by lazy { entity.data<ShipData>() }

    private val replenishRate = 0.5f
    private var lastReplenishedTime = 0.0f
    private var replenishAmount = 0.2f

    override fun beforeUpdate(time: Float, deltaTime: Float) {
        if (!entity.active) {
            return
        }
        if (time - lastReplenishedTime > replenishRate) {
            shipData.replenishEnergy(replenishAmount)
            lastReplenishedTime = time
        }
    }
}

