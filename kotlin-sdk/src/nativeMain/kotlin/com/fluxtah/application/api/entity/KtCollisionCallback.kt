package com.fluxtah.application.api.entity

import com.fluxtah.application.api.interop.c_getEntityCollisionInfo
import com.fluxtah.application.api.interop.model.CCollisionResult
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.scene.EntityInfo
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.*
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
    val sourceEntityInfo = collisionResult.sourceEntityInfo!!.asStableRef<EntityInfo>().get()
    val targetEntityInfo = collisionResult.targetEntityInfo!!.asStableRef<EntityInfo>().get()
    if (!sourceEntityInfo.entity.active || !targetEntityInfo.entity.active) return

    val volumeResults = mutableListOf<BoundingVolumeCollisionResult>()

    for (i in 0 until collisionResult.numResults) {
        val vResult = collisionResult.results[i]
        volumeResults.add(
            BoundingVolumeCollisionResult(
                vResult.sourceVolumeIndex,
                vResult.targetVolumeIndex
            )
        )
    }

    val result = KotlinCollisionResult(
        sourceEntityInfo.entity,
        targetEntityInfo.entity,
        volumeResults
    )

    activeSceneInfo.onCollision?.invoke(activeSceneInfo.scene, result)

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
    val normal: Vector3
)