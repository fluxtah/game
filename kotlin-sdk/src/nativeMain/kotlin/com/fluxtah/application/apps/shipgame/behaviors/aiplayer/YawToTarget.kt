package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipInput

fun yawToTarget(entity: Entity, targetEntity: Entity, shipActions: ShipInput) {
    val entityForward = entity.getOrientation().getLocalForwardAxis()
    val toTarget = (Vector3(targetEntity.positionX, targetEntity.positionY, targetEntity.positionZ) - Vector3(
        entity.positionX,
        entity.positionY,
        entity.positionZ
    )).normalized()

    val crossProduct = entityForward.cross(toTarget)
    val dotProduct = entityForward.dot(toTarget)

    val alignmentThreshold = 0.95 // Adjust this value as needed

    if (dotProduct < alignmentThreshold) {
        shipActions.isYawingLeft = crossProduct.y > 0
        shipActions.isYawingRight = crossProduct.y < 0
    } else {
        // They are aligned or close enough, stop yawing
        shipActions.isYawingLeft = false
        shipActions.isYawingRight = false
    }

}