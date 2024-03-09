package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import com.fluxtah.application.apps.shipgame.scenes.main.data.Team
import kotlin.math.abs

class RechargeNearbyBehaviour : EntityBehavior() {
    private val ships: List<Entity> by lazy { scene.entitiesInPool(Id.ENT_PLAYER_SHIP) }
    private val data by lazy { entity.data<PowerNodeData>() }
    private val sceneData by lazy { scene.data<GameData>() }

    private val shipsToUpdate = mutableListOf<Entity>()
    private val shipsToDeplete = mutableListOf<Entity>()

    override fun beforeUpdate(time: Float, deltaTime: Float) {
        shipsToUpdate.clear()
        shipsToDeplete.clear()
        for (ship in ships) {
            if (!ship.active) continue
            val distanceToShip = abs(
                Vector3.distanceBetween(
                    ship.positionX,
                    ship.positionY,
                    ship.positionZ,
                    entity.positionX,
                    entity.positionY,
                    entity.positionZ
                )
            )

            if (ship.data<ShipData>().playerData.team == data.team) {
                if (distanceToShip < data.rechargeDistance && (time - data.lastRechargeTime) > data.rechargeRate) {
                    shipsToUpdate.add(ship)
                }
            } else {
                if (distanceToShip < data.rechargeDistance && (time - data.lastRechargeTime) > data.rechargeRate) {
                    shipsToDeplete.add(ship)
                }

            }
        }

        if (shipsToUpdate.isEmpty() && shipsToDeplete.isEmpty()) return

        for (ship in shipsToUpdate) {
            ship.data<ShipData>().replenishEnergy(data.replenishAmount)
        }
        for (ship in shipsToDeplete) {
            val depleteAmount = data.replenishAmount * 0.9f
            val enemyShipData = ship.data<ShipData>()
            enemyShipData.depleteEnergy(depleteAmount) // only suck half the energy
            if (enemyShipData.playerData.team == Team.Friendly) {
                sceneData.enemyTeam.score += depleteAmount
            } else {
                sceneData.friendlyTeam.score += depleteAmount
            }
        }

        data.lastRechargeTime = time
    }
}
