package com.fluxtah.application.apps.shipgame

object CollisionGroups {
    // Groups
    const val GROUP_MAP_BLOCK = 1 shl 0
    const val GROUP_PLAYER = 1 shl 1
    const val GROUP_PROJECTILE = 1 shl 2
    const val GROUP_DESTROYABLE = 1 shl 3

    // Masks
    const val MASK_MAP_BLOCK = GROUP_PLAYER or GROUP_PROJECTILE
    const val MASK_PLAYER = GROUP_MAP_BLOCK or GROUP_PROJECTILE or GROUP_DESTROYABLE or GROUP_PLAYER
    const val MASK_PROJECTILE = GROUP_MAP_BLOCK or GROUP_PLAYER or GROUP_DESTROYABLE
    const val MASK_DESTROYABLE = GROUP_PLAYER or GROUP_PROJECTILE
}