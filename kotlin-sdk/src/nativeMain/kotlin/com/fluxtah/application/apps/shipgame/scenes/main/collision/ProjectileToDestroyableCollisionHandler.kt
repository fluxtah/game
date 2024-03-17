package com.fluxtah.application.apps.shipgame.scenes.main.collision

import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.entity.BoundingVolumeCollisionResult
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.AsteroidDieBehavior

class ProjectileToDestroyableCollisionHandler :
    CollisionHandler(CollisionGroups.GROUP_PROJECTILE, CollisionGroups.GROUP_ASTEROID) {
    override fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        results: List<BoundingVolumeCollisionResult>
    ) {
        if (targetEntity.id == Id.ENT_ASTEROID) {
            val asteroidDieBehavior = targetEntity.getBehaviorByType<AsteroidDieBehavior>()
            scene.entityToPool(sourceEntity)
            asteroidDieBehavior.die()
            sourceEntity.visible = false
        }
    }
}