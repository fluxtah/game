package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class PlacePowerNodeSchedule : AiSchedule() {
    private val shipData: ShipData by lazy { entity.data() }
    var done = false

    override fun enter() {
        done = false
    }

    override fun update(time: Float, deltaTime: Float) {
        if (done) {
            return
        }

        if (shipData.energy < 70.0f) {
            if(shipData.energy > 50.0f) {
                machine.changeState(AiPlayerShipState.MoveToEnemy)
            } else {
                machine.changeState(AiPlayerShipState.SeekPowerNode)
            }
            done = true
            return
        }

        val shipActions = shipData.input

        shipActions.isPlacingPowerNode = true

        machine.changeStateAt(AiPlayerShipState.MoveToEnemy, time + 0.5f)
        done = true
    }

    override fun exit() {
        shipData.input.isPlacingPowerNode = false
    }
}
