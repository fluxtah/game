package com.fluxtah.application.apps.shipgame.components

import com.fluxtah.application.api.input.Key
import com.fluxtah.application.api.isKeyPressed
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class PlayerInputComponent : SceneComponent() {

    private val sceneData by lazy { scene.data<GameData>() }

    var playerShipData: ShipData? = null

    override fun onBeforeSceneUpdate(time: Float, deltaTime: Float) {
        val playerShipData = playerShipData ?: return

        if (sceneData.gameTimeRemaining <= 0) {
            return
        }

        playerShipData.input.isFiring = isKeyPressed(Key.Space)
        playerShipData.input.isThrusting = isKeyPressed(Key.Up)
        playerShipData.input.isYawingLeft = isKeyPressed(Key.Left)
        playerShipData.input.isYawingRight = isKeyPressed(Key.Right)
        playerShipData.input.isMovingForward = isKeyPressed(Key.W)
        playerShipData.input.isReversing = isKeyPressed(Key.S)
        playerShipData.input.isMovingLeft = isKeyPressed(Key.A)
        playerShipData.input.isMovingRight = isKeyPressed(Key.D)
        playerShipData.input.isPlacingPowerNode = isKeyPressed(Key.P)

        playerShipData.input.isBoosting = isKeyPressed(Key.B)
    }
}

fun SceneBuilder.playerInputComponent() {
    component(Id.COMPONENT_PLAYER_INPUT) {
        PlayerInputComponent()
    }
}