package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.math.Vector3
import kotlin.math.atan2

fun Entity.angleToEntity(toEntity: Entity): Float {
    // Get player and AI ship positions
    val playerPosition = Vector3(toEntity.positionX, toEntity.positionY, toEntity.positionZ)
    val aiPosition = Vector3(positionX, positionY, positionZ)

    // Calculate direction from AI to player
    val directionToPlayer = playerPosition - aiPosition

    // Calculate the angle to the player relative to the world's forward direction
    // Assuming 0 angle is facing along the Z-axis, adjust if your game uses a different orientation
    val angleToPlayer = atan2(directionToPlayer.x.toDouble(), directionToPlayer.z.toDouble()).toFloat()

    return angleToPlayer
}