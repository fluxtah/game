package com.fluxtah.application.apps.shipgame.scenes.main.collision

import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.entity.BoundingVolumeCollisionResult
import com.fluxtah.application.api.entity.CollisionContactPoint
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.ShipDieBehavior
import com.fluxtah.application.apps.shipgame.components.ui.GameUiComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PlasmaBoltData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class ProjectileToPlayerShipCollisionHandler :
    CollisionHandler(CollisionGroups.GROUP_PROJECTILE, CollisionGroups.GROUP_PLAYER) {
    override fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        contactPoints: List<CollisionContactPoint>
    ) {
        val data = sourceEntity.data<PlasmaBoltData>()
        val gameUiComponent = scene.componentById<GameUiComponent>(Id.COMPONENT_GAME_UI)
        val boltOwner = data.owner!!

        val otherEntityData: ShipData = targetEntity.data()

        if (sourceEntity.data is PlasmaBoltData &&
            targetEntity.data is ShipData &&
            data.team != otherEntityData.playerData.team
        ) {
            if (otherEntityData.depleteShieldThenArmor(scene.data<GameData>().plasmaBoltDamage) > 0) {
                boltOwner.apply {
                    val ownerData = this.data<ShipData>()
                    val gameData = scene.data<GameData>()
                    val teamData =
                        if (ownerData.playerData.team == gameData.friendlyTeam.team) gameData.friendlyTeam else gameData.enemyTeam
                    ownerData.playerData.kills++
                    ownerData.energy += gameData.enemyShipKillEnergyBonus
                    teamData.score += gameData.enemyShipKillEnergyBonus
                    gameUiComponent.addNotification(
                        "${ownerData.playerData.name} @ ${otherEntityData.playerData.name} +${gameData.enemyShipKillEnergyBonus.toInt()}",
                    )
                }
                targetEntity.getBehaviorByType<ShipDieBehavior>().die(boltOwner)
            }

            scene.entityToPool(sourceEntity)
            sourceEntity.visible = false
        }
    }
}