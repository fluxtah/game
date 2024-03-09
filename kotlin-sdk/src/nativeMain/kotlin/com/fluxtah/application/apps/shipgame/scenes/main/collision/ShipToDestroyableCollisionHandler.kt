package com.fluxtah.application.apps.shipgame.scenes.main.collision

import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.entity.BoundingVolumeCollisionResult
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.AsteroidDieBehavior
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import com.fluxtah.application.apps.shipgame.scenes.main.sequences.SequencePlayerDeathRespawnArgs

class ShipToDestroyableCollisionHandler : CollisionHandler(
    CollisionGroups.GROUP_PLAYER, CollisionGroups.GROUP_DESTROYABLE
) {
    override fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        results: List<BoundingVolumeCollisionResult>
    ) {
        if (sourceEntity.data is ShipData && targetEntity.id == Id.ENT_ASTEROID) {
            val blockDieBehavior = targetEntity.getBehaviorByType<AsteroidDieBehavior>()
            blockDieBehavior.die()
            sourceEntity.active = false
            sourceEntity.visible = false
            sourceEntity.resetBehaviors()
            scene.createSequence(Id.SEQ_DEATH_RESPAWN)?.play(SequencePlayerDeathRespawnArgs(
                ship = sourceEntity
            ))
        }
    }
}