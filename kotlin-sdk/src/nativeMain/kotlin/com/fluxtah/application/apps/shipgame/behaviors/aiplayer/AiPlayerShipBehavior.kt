package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiEntityStateMachine
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameSceneState
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class AiPlayerShipBehavior : EntityBehavior() {
    private val ai by lazy { AiEntityStateMachine(AiPlayerShipState.Idle, scene, entity) }
    private val data: ShipData by lazy { entity.data() }
    private val sceneData: GameData by lazy { scene.data() }

    override fun initialize() {
        reset()
    }

    override fun reset() {
        ai.resetAndClearSchedules()

        ai.addSchedule(AiPlayerShipState.Idle, IdleSchedule())
        ai.addSchedule(AiPlayerShipState.MoveToEnemy, MoveToEnemySchedule())
        ai.addSchedule(AiPlayerShipState.AttackEnemy, AttackEnemySchedule())
        ai.addSchedule(AiPlayerShipState.PlacePowerNode, PlacePowerNodeSchedule())
        ai.addSchedule(AiPlayerShipState.Defending, DefendingSchedule())
        ai.addSchedule(AiPlayerShipState.AttackPowerNode, AttackPowerNodeSchedule())
        ai.addSchedule(AiPlayerShipState.SeekPowerNode, SeekPowerNodeSchedule())
        ai.addSchedule(AiPlayerShipState.FleeEnemy, FleeEnemySchedule())
    }

    override fun update(time: Float) {
        if (sceneData.sceneState != GameSceneState.Playing) {
            data.input.stop()
            return
        }

        if (!data.playerData.isBot) {
            return
        }

        ai.update(time, fixedTimeStep)
    }
}
