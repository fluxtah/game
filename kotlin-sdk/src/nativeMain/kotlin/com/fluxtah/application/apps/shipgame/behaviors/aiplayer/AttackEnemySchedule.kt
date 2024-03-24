package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.api.ai.BooleanAlternator
import com.fluxtah.application.api.ai.Timer
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.PI

class AttackEnemySchedule : AiSchedule() {
    private val booleanAlternatorStrafe = BooleanAlternator(2.0f, 0.5f)
    private val checkConditionTimer = Timer(5.0f)
    private val shipData: ShipData by lazy { entity.data() }
    private val sceneData by lazy { scene.data<GameData>() }

    private val shipPool by lazy { scene.entitiesInPool(Id.ENT_PLAYER_SHIP) }

    override fun enter() {
        checkConditionTimer.start()
    }

    override fun exit() {
        checkConditionTimer.stop()
    }

    override fun update(time: Float, deltaTime: Float) {
        val shipActions = shipData.input

        val enemies = shipPool.filter {
            it.active && it.data<ShipData>().playerData.team != shipData.playerData.team
        }

        val closestEnemy = enemies.minByOrNull {
            Vector3.distanceBetween(
                entity.positionX,
                entity.positionY,
                entity.positionZ,
                it.positionX,
                it.positionY,
                it.positionZ
            )
        }

        if (closestEnemy == null) {
            machine.changeState(AiPlayerShipState.Idle)
            return
        }

        val distanceToEnemy = Vector3.distanceBetween(
            entity.positionX,
            entity.positionY,
            entity.positionZ,
            closestEnemy.positionX,
            closestEnemy.positionY,
            closestEnemy.positionZ
        )

        val combatRange = sceneData.aiShipsCombatRange
        val shootingRange = sceneData.aiShipsCombatRange * 2f

        // Determine movement based on distance and current velocity
        if (distanceToEnemy > combatRange) {
            machine.changeState(AiPlayerShipState.MoveToEnemy)
            return
        } else if (distanceToEnemy < combatRange) {
            shipActions.isMovingForward = false
            shipActions.isReversing = false
            booleanAlternatorStrafe.update(deltaTime) {
                shipActions.isMovingRight = it
            }
        } else {
            // Maintain current velocity if within combat range
            // Avoid abrupt changes by not toggling flags
            shipActions.isMovingForward = entity.velocityZ > 0
            shipActions.isReversing = entity.velocityZ < 0
            shipActions.isMovingRight = false
        }

        if (distanceToEnemy <= shootingRange) {
            shipActions.isThrusting = closestEnemy.positionY > entity.positionY
            shipActions.isFiring = true
        } else {
            shipActions.isFiring = false
        }

        val entityForward = entity.getOrientation().getLocalForwardAxis()
        val toTarget =
            (Vector3(closestEnemy.positionX, closestEnemy.positionY, closestEnemy.positionZ) - Vector3(
                entity.positionX,
                entity.positionY,
                entity.positionZ
            )).normalized()

        val crossProduct = entityForward.cross(toTarget)
        shipActions.isYawingLeft = crossProduct.y > 0
        shipActions.isYawingRight = crossProduct.y < 0

        checkConditionTimer.update(deltaTime) {
            if (shipData.energy < 20f) {
                machine.changeState(AiPlayerShipState.SeekPowerNode)
            } else if (shipData.energy > sceneData.powerNodeCost) {
                machine.changeState(AiPlayerShipState.PlacePowerNode)
            } else {
                machine.changeState(AiPlayerShipState.AttackPowerNode)
            }
        }
    }
}