package com.fluxtah.application.api.collision

import com.fluxtah.application.api.entity.BoundingVolumeCollisionResult
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.KotlinCollisionResult
import com.fluxtah.application.api.scene.Scene

/**
 * A collision handler that can be used to handle collisions between entities by collision group.
 *
 * @param groupA The first collision group
 * @param groupB The second collision group
 */
abstract class CollisionHandler(private val groupA: Int, private val groupB: Int) {
    fun handleCollision(scene: Scene, result: KotlinCollisionResult): Boolean {
        // Switch the source and target if the source is groupB and the target is groupA
        if (result.sourceEntity.collisionGroup == groupA
            && result.targetEntity.collisionGroup == groupB
        ) {
            onHandleCollision(scene, result.sourceEntity, result.targetEntity, result.results.map {
                BoundingVolumeCollisionResult(
                    it.sourceVolumeIndex,
                    it.targetVolumeIndex
                )
            })
            return true
        } else if (result.sourceEntity.collisionGroup == groupB
            && result.targetEntity.collisionGroup == groupA
        ) {
            onHandleCollision(scene, result.targetEntity, result.sourceEntity, result.results.map {
                BoundingVolumeCollisionResult(
                    it.targetVolumeIndex,
                    it.sourceVolumeIndex
                )
            })
            return true
        }

        return false
    }

    protected abstract fun onHandleCollision(
        scene: Scene,
        sourceEntity: Entity,
        targetEntity: Entity,
        results: List<BoundingVolumeCollisionResult>
    )
}