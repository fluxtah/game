package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.ai.AiSchedule
import com.fluxtah.application.api.ai.BooleanAlternator
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import com.fluxtah.application.apps.shipgame.scenes.main.data.Team
import kotlin.math.PI
import kotlin.random.Random

class MoveToEnemySchedule : AiSchedule() {
    private val booleanAlternator = BooleanAlternator(0.25f, 0.5f)
    private val shipData: ShipData by lazy { entity.data() }
    private val sceneData by lazy { scene.data<GameData>() }
    private val shipPool by lazy { scene.entitiesInPool(Id.ENT_PLAYER_SHIP) }
    private val teamData by lazy {
        if (shipData.playerData.team == Team.Friendly) {
            sceneData.friendlyTeam
        } else {
            sceneData.enemyTeam
        }
    }

    override fun update(time: Float, deltaTime: Float) {
        val shipActions = shipData.input

        val enemies = shipPool.filter {
            it.active && it.data<ShipData>().playerData.team != shipData.playerData.team
        }

        if (enemies.isEmpty()) {
            machine.changeState(AiPlayerShipState.PlacePowerNode)
            return
        }

        val closestEnemy = enemies.minBy {
            Vector3.distanceBetween(
                entity.positionX,
                entity.positionY,
                entity.positionZ,
                it.positionX,
                it.positionY,
                it.positionZ
            )
        }

        val distanceToEnemy = Vector3.distanceBetween(
            entity.positionX,
            entity.positionY,
            entity.positionZ,
            closestEnemy.positionX,
            closestEnemy.positionY,
            closestEnemy.positionZ
        )

        val angleToEnemy = entity.angleToEntity(closestEnemy)
        val combatRange = sceneData.aiShipsCombatRange * 0.75f

        val currentYaw =
            entity.rotationY.toRadians() // Assuming rotationY is the yaw and is in degrees, convert to radians

        // Calculate the shortest direction to turn towards the player
        val angleDifference = (angleToEnemy - currentYaw + PI * 3) % (PI * 2) - PI
        shipActions.isYawingLeft = angleDifference > 0
        shipActions.isYawingRight = angleDifference < 0

        // Determine movement based on distance and current velocity
        if (distanceToEnemy > combatRange) {
            // Move forward if farther than combat range
            booleanAlternator.update(deltaTime) {
                shipActions.isMovingForward = it
            }
            shipActions.isReversing = false
            shipActions.isMovingRight = false
        } else {
            shipActions.isReversing = false
            shipActions.isMovingRight = false
            shipActions.isMovingForward = false

            if (teamData.placePowerNodeCoolDownTimer > sceneData.placePowerNodeCoolDown &&
                shipData.energy > sceneData.powerNodeCost
            ) {
                machine.changeState(AiPlayerShipState.PlacePowerNode)
            } else {
                if(Random.nextBoolean()) {
                    machine.changeState(AiPlayerShipState.AttackPowerNode)
                } else {
                    machine.changeState(AiPlayerShipState.AttackEnemy)
                }
            }
        }
    }
}
