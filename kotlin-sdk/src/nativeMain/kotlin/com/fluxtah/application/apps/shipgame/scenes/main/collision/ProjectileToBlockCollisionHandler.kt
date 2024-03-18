package com.fluxtah.application.apps.shipgame.scenes.main.collision

import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.entity.BoundingVolumeCollisionResult
import com.fluxtah.application.api.entity.CollisionContactPoint
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.PowerNodeDeathBehavior
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.components.ui.GameUiComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PlasmaBoltData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import com.fluxtah.application.apps.shipgame.scenes.main.data.Team

class ProjectileToBlockCollisionHandler :
    CollisionHandler(CollisionGroups.GROUP_PROJECTILE, CollisionGroups.GROUP_MAP_BLOCK) {
    override fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        contactPoints: List<CollisionContactPoint>
    ) {
        if (targetEntity.data is PowerNodeData) {
            val powerNodeData = targetEntity.data<PowerNodeData>()
            val plasmaBoltData = sourceEntity.data<PlasmaBoltData>()

           // println("Power node hit by plasma bolt team: ${plasmaBoltData.team}, node team: ${powerNodeData.team}, health: ${powerNodeData.health}")
            if(plasmaBoltData.team == powerNodeData.team) {
                scene.entityToPool(sourceEntity) // absorb friendly plasma bolts
                sourceEntity.visible = false
                return
            }

            if (powerNodeData.depleteHealth(4f) > 0) {
                val sceneData = scene.data<GameData>()
                val killersTeam = if (plasmaBoltData.team == Team.Friendly) {
                    sceneData.friendlyTeam
                } else {
                    sceneData.enemyTeam

                }
                killersTeam.score += sceneData.powerNodeKillEnergyBonus
                scene.entitiesInPool(Id.ENT_PLAYER_SHIP).forEach {
                    val shipData = it.data<ShipData>()
                    if (it.active && shipData.playerData.team == killersTeam.team) {
                        shipData.energy += sceneData.powerNodeKillEnergyBonus
                    }
                }
                val powerNodeDeath = targetEntity.getBehaviorByType<PowerNodeDeathBehavior>()

                val uiComponent = scene.componentById<GameUiComponent>(Id.COMPONENT_GAME_UI)
                uiComponent.addNotification("${plasmaBoltData.team} +${sceneData.powerNodeKillEnergyBonus.toInt()}")

                val mapComponent = scene.componentById<MapComponent>(Id.COMPONENT_MAP)
                mapComponent.clearDataAtWorldPosition(targetEntity.positionX, targetEntity.positionZ)

                powerNodeDeath.die()
            }
        }

        scene.entityToPool(sourceEntity)
        sourceEntity.visible = false

    }
}