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

class FleeEnemySchedule : AiSchedule() {
    private val booleanAlternator = BooleanAlternator(0.25f, 0.5f)
    private val timer = Timer(5.0f)
    private val shipData: ShipData by lazy { entity.data() }
    private val sceneData by lazy { scene.data<GameData>() }
    private val shipPool by lazy { scene.entitiesInPool(Id.ENT_PLAYER_SHIP) }

    override fun enter() {
        shipData.input.isFiring = false
        timer.start()
    }

    override fun exit() {
        timer.stop()
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
        val fleeDistance = 256f

        if (distanceToEnemy > fleeDistance) {
            machine.changeState(AiPlayerShipState.Idle)
            return
        }

        val currentYaw = entity.rotationY.toRadians()

        val angleDifference = ((angleToEnemy + PI) - currentYaw + PI * 3) % (PI * 2) - PI
        shipActions.isYawingLeft = angleDifference > 0
        shipActions.isYawingRight = angleDifference < 0

        shipActions.isMovingForward = true
        shipActions.isReversing = false
        shipActions.isMovingRight = false

        timer.update(deltaTime) {
            shipActions.isMovingForward = false
            machine.changeState(AiPlayerShipState.Idle)
        }
    }
}
