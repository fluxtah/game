package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.api.ai.Timer
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.PI

class SeekPowerNodeSchedule : AiSchedule() {
    private val checkConditionTimer = Timer(5.0f)
    private val shipData: ShipData by lazy { entity.data() }
    private val sceneData by lazy { scene.data<GameData>() }

    private val powerNodesPool by lazy { scene.entitiesInPool(MapComponent.ENTITY_POOL_BLOCK_POWER) }

    override fun enter() {
        checkConditionTimer.start()
    }

    override fun exit() {
        shipData.input.stop()
        checkConditionTimer.stop()
    }

    override fun update(time: Float, deltaTime: Float) {
        val shipActions = shipData.input

        val friendlyPowerNodes = powerNodesPool.filter {
            it.active && it.data<PowerNodeData>().team == shipData.playerData.team
        }

        if (friendlyPowerNodes.isEmpty()) {
            if (shipData.energy > 20f) {
                machine.changeState(AiPlayerShipState.AttackEnemy)
            } else {
                machine.changeState(AiPlayerShipState.FleeEnemy)
            }
            return
        }

        val closestPowerNode = friendlyPowerNodes.minBy {
            Vector3.distanceBetween(
                entity.positionX,
                entity.positionY,
                entity.positionZ,
                it.positionX,
                it.positionY,
                it.positionZ
            )
        }

        val distanceToPowerNode = Vector3.distanceBetween(
            entity.positionX,
            entity.positionY,
            entity.positionZ,
            closestPowerNode.positionX,
            closestPowerNode.positionY,
            closestPowerNode.positionZ
        )

        val rechargeDistance = closestPowerNode.data<PowerNodeData>().rechargeDistance * 0.5f
        // Determine movement based on distance and current velocity
        if (distanceToPowerNode > rechargeDistance) {
            shipActions.isMovingForward = true
            shipActions.isReversing = false
        } else if (distanceToPowerNode < rechargeDistance) {
            shipActions.isMovingForward = false
            shipActions.isReversing = true
            shipActions.isMovingRight = false
        }

        val entityForward = entity.getOrientation().getLocalForwardAxis()
        val toTarget =
            (Vector3(closestPowerNode.positionX, closestPowerNode.positionY, closestPowerNode.positionZ) - Vector3(
                entity.positionX,
                entity.positionY,
                entity.positionZ
            )).normalized()

        val crossProduct = entityForward.cross(toTarget)
        shipActions.isYawingLeft = crossProduct.y > 0
        shipActions.isYawingRight = crossProduct.y < 0

        checkConditionTimer.update(deltaTime) {
            machine.changeState(AiPlayerShipState.MoveToEnemy)
        }
    }
}