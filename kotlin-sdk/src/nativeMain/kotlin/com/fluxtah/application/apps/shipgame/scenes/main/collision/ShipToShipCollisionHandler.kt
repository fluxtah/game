package com.fluxtah.application.apps.shipgame.scenes.main.collision

import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.entity.CollisionContactPoint
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.behaviors.ShipCollisionResponseBehavior
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class ShipToShipCollisionHandler : CollisionHandler(
    CollisionGroups.GROUP_PLAYER, CollisionGroups.GROUP_PLAYER
) {
    override fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        contactPoints: List<CollisionContactPoint>
    ) {
        if(sourceEntity.data is ShipData && targetEntity.data is ShipData) {
            sourceEntity.getBehaviorByType<ShipCollisionResponseBehavior>()
                // TODO need to consider all colliding volumes
                .handleResponse(scene, targetEntity, contactPoints)
        }
    }
}