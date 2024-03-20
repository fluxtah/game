package com.fluxtah.application.api.entity

import com.fluxtah.application.api.interop.c_getEntityCollisionInfo
import com.fluxtah.application.api.interop.model.CCollisionResult
import com.fluxtah.application.api.interop.model.CCollisionResult2
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.scene.EntityInfo
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.get
import kotlinx.cinterop.useContents
import kotlin.experimental.ExperimentalNativeApi

data class BoundingVolumeCollisionResult(
    val sourceVolumeIndex: Int,
    val targetVolumeIndex: Int
)

data class KotlinCollisionResult(
    val sourceEntity: Entity,
    val targetEntity: Entity,
    val results: List<BoundingVolumeCollisionResult>,
)

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktCollisionCallback")
fun ktCollisionCallback(collisionResult: CCollisionResult) {
//    val sourceEntityInfo = collisionResult.sourceEntityInfo!!.asStableRef<EntityInfo>().get()
//    val targetEntityInfo = collisionResult.targetEntityInfo!!.asStableRef<EntityInfo>().get()
//    if (!sourceEntityInfo.entity.active || !targetEntityInfo.entity.active) return
//
//    val volumeResults = mutableListOf<BoundingVolumeCollisionResult>()
//
//    for (i in 0 until collisionResult.numResults) {
//        val vResult = collisionResult.results[i]
//        volumeResults.add(
//            BoundingVolumeCollisionResult(
//                vResult.sourceVolumeIndex,
//                vResult.targetVolumeIndex
//            )
//        )
//    }
//
//    val result = KotlinCollisionResult(
//        sourceEntityInfo.entity,
//        targetEntityInfo.entity,
//        volumeResults
//    )
//
//    activeSceneInfo.onCollision?.invoke(activeSceneInfo.scene, result)

}

fun getEntityCollisionInfo(a: Entity, b: Entity, aAabbIndex: Int = 0, bAabbIndex: Int = 0): CollisionInfo {
    @OptIn(ExperimentalForeignApi::class)
    return c_getEntityCollisionInfo!!.invoke(a.handle, b.handle, aAabbIndex, bAabbIndex).useContents {
        CollisionInfo(
            Vector3(penetration[0], penetration[1], penetration[2]),
            Vector3(normal[0], normal[1], normal[2])
        )
    }
}

data class CollisionInfo(
    val penetration: Vector3,
    val collisionNormal: Vector3
)

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