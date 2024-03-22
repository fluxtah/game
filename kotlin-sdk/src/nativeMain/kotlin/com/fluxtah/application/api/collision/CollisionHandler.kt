package com.fluxtah.application.api.collision

import com.fluxtah.application.api.entity.CollisionContactPoint
import com.fluxtah.application.api.entity.CollisionResult2
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene

/**
 * A collision handler that can be used to handle collisions between entities by collision group.
 *
 * @param groupA The first collision group
 * @param groupB The second collision group
 */
abstract class CollisionHandler(private val groupA: Int, private val groupB: Int) {
    fun handleCollision(scene: Scene, result: CollisionResult2): Boolean {
        // Switch the source and target if the source is groupB and the target is groupA
        if (result.sourceEntity.collisionGroup == groupA
            && result.targetEntity.collisionGroup == groupB
        ) {
            onHandleCollision(scene, result.sourceEntity, result.targetEntity, result.contactPoints)
            return true
        } else if (result.sourceEntity.collisionGroup == groupB
            && result.targetEntity.collisionGroup == groupA
        ) {
            onHandleCollision(scene, result.targetEntity, result.sourceEntity, result.contactPoints.map {
                it.copy(
                    positionA = it.positionB,
                    positionB = it.positionA,
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
        contactPoints: List<CollisionContactPoint>
    )
}