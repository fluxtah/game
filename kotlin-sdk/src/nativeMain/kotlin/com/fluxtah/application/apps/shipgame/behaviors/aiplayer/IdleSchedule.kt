package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameSceneState
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.random.Random

class IdleSchedule : AiSchedule() {
    private val sceneData by lazy { scene.data<GameData>() }
    private val shipActions by lazy { entity.data<ShipData>().input }

    private var done = false

    override fun enter() {
        done = false
    }

    override fun update(time: Float, deltaTime: Float) {
        if (done) {
            return
        }

        shipActions.stop()

        if (sceneData.sceneState == GameSceneState.Playing) {
            machine.changeStateAt(AiPlayerShipState.MoveToEnemy, time + (Random.nextFloat() * 3.0f))
            done = true
        }
    }
}