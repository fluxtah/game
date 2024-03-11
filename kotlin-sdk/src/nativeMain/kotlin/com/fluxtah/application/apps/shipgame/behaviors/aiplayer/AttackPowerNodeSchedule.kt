package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.api.ai.BooleanAlternator
import com.fluxtah.application.api.ai.Timer
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.PI

class AttackPowerNodeSchedule : AiSchedule() {
    private val booleanAlternatorStrafe = BooleanAlternator(2.0f, 0.5f)
    private val checkConditionTimer = Timer(10.0f)
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

        val enemyPowerNodes = powerNodesPool.filter {
            it.active && it.data<PowerNodeData>().team != shipData.playerData.team
        }

        if(enemyPowerNodes.isEmpty()) {
            machine.changeState(AiPlayerShipState.MoveToEnemy)
            return
        }

        val closestPowerNode = enemyPowerNodes.minBy {
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

        val angleToPowerNode = entity.angleToEntity(closestPowerNode)
        val combatRange = sceneData.aiShipsCombatRange
        val shootingRange = sceneData.aiShipsCombatRange * 2f

        // Determine movement based on distance and current velocity
        if (distanceToPowerNode > combatRange) {
            shipActions.isMovingForward = true
            shipActions.isReversing = false
        } else if (distanceToPowerNode < combatRange) {
            shipActions.isMovingForward = false
            shipActions.isReversing = false
            shipActions.isMovingRight = false
            booleanAlternatorStrafe.update(deltaTime) {
                shipActions.isMovingLeft = it
            }
        } else {
            // Maintain current velocity if within combat range
            // Avoid abrupt changes by not toggling flags
            shipActions.isMovingForward = entity.velocityZ > 0
            shipActions.isReversing = entity.velocityZ < 0
            shipActions.isMovingRight = false
            shipActions.isMovingLeft = false
        }

        if (distanceToPowerNode <= shootingRange) {
            shipActions.isThrusting = false
            shipActions.isFiring = true
        } else {
            shipActions.isFiring = false
        }

        val currentYaw =
            entity.rotationY.toRadians() // Assuming rotationY is the yaw and is in degrees, convert to radians

        // Calculate the shortest direction to turn towards the player
        val angleDifference = (angleToPowerNode - currentYaw + PI * 3) % (PI * 2) - PI
        shipActions.isYawingLeft = angleDifference > 0
        shipActions.isYawingRight = angleDifference < 0

        checkConditionTimer.update(deltaTime) {
            machine.changeState(AiPlayerShipState.AttackEnemy)
        }
    }
}