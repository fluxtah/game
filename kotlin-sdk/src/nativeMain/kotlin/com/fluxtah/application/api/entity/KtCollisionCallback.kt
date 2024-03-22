package com.fluxtah.application.api.entity

import com.fluxtah.application.api.interop.model.CCollisionResult2
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.scene.EntityInfo
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.get

data class CollisionContactPoint(
    val positionA: Vector3,
    val positionB: Vector3,
    val normal: Vector3,
    val distance: Float
)

data class CollisionResult2(
    val sourceEntity: Entity,
    val targetEntity: Entity,
    val contactPoints: List<CollisionContactPoint> = emptyList()
)

@OptIn(ExperimentalForeignApi::class)
fun ktCollisionCallback2(collisionResult: CCollisionResult2) {
    val sourceEntityInfo = collisionResult.userPointerA!!.asStableRef<EntityInfo>().get()
    val targetEntityInfo = collisionResult.userPointerB!!.asStableRef<EntityInfo>().get()
    if (!sourceEntityInfo.entity.active || !targetEntityInfo.entity.active) return

    // println("COLLISION: Source entity: ${sourceEntityInfo.entity.id} with  ${targetEntityInfo.entity.id}")

    val contactPoints = mutableListOf<CollisionContactPoint>()

    for (i in 0 until collisionResult.numContacts) {
        val cContactPoint = collisionResult.contactPoints[i]
        // println("COLLISION: Contact distance: ${cContactPoint.distance}")
        contactPoints.add(
            CollisionContactPoint(
                distance = cContactPoint.distance,
                positionA = Vector3(cContactPoint.positionAX, cContactPoint.positionAY, cContactPoint.positionAZ),
                positionB = Vector3(cContactPoint.positionBX, cContactPoint.positionBY, cContactPoint.positionBZ),
                normal = Vector3(
                    cContactPoint.collisionNormalX,
                    cContactPoint.collisionNormalY,
                    cContactPoint.collisionNormalZ
                ),
            )
        )
    }

    val result = CollisionResult2(
        sourceEntityInfo.entity,
        targetEntityInfo.entity,
        contactPoints
    )

    activeSceneInfo.onCollision?.invoke(activeSceneInfo.scene, result)

}