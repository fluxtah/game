package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class PlacePowerNodeBehavior : EntityBehavior() {
    val data by lazy { entity.data<ShipData>() }
    val map by lazy { scene.componentById<MapComponent>(Id.COMPONENT_MAP) }
    val sceneData by lazy { scene.data<GameData>() }

    override fun update(time: Float) {
        if (data.input.isPlacingPowerNode) {
            // Need energy to place power node
            val playerTeamData = if(data.playerData.team == sceneData.friendlyTeam.team) sceneData.friendlyTeam else sceneData.enemyTeam
            if (data.energy >= sceneData.powerNodeCost &&
                playerTeamData.placePowerNodeCoolDownTimer > sceneData.placePowerNodeCoolDown) {
                playerTeamData.placePowerNodeCoolDownTimer = 0f
               //  println("${data.playerData.name} is placing a power node at ${entity.positionX}, ${entity.positionZ}")
                if (map.placePowerNode(entity.positionX, entity.positionZ, playerTeamData.team)) {
                    // Deduct energy
                    data.energy -= sceneData.powerNodeCost
                }
            }
        }
    }
}