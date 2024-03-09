package com.fluxtah.application.apps.shipgame.scenes.main.data

data class ShipInput(
    var isThrusting: Boolean = false,
    var isFiring: Boolean = false,
    var isYawingLeft: Boolean = false,
    var isYawingRight: Boolean = false,
    var isMovingForward: Boolean = false,
    var isReversing: Boolean = false,
    var isMovingLeft: Boolean = false,
    var isMovingRight: Boolean = false,
    var isPlacingPowerNode: Boolean = false
) {
    fun stop() {
        isThrusting = false
        isFiring = false
        isYawingLeft = false
        isYawingRight = false
        isMovingForward = false
        isReversing = false
        isMovingLeft = false
        isMovingRight = false
        isPlacingPowerNode = false
    }
}