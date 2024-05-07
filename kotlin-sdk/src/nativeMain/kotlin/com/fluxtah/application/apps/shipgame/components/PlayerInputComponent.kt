package com.fluxtah.application.apps.shipgame.components

import com.fluxtah.application.api.getJoystickAxes
import com.fluxtah.application.api.getJoystickButtons
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

        val axes = getJoystickAxes(0)
        val buttons = getJoystickButtons(0)

        if(axes != null) {
            if (axes.axes[0] > 0.5) {
                playerShipData.input.isMovingRight = true
            } else if (axes.axes[0] < -0.5) {
                playerShipData.input.isMovingLeft = true
            } else {
                playerShipData.input.isMovingRight = false
                playerShipData.input.isMovingLeft = false
            }

            if(axes.axes[1] > 0.5) {
                playerShipData.input.isReversing = true
            } else if(axes.axes[1] < -0.5) {
                playerShipData.input.isMovingForward = true
            } else {
                playerShipData.input.isMovingForward = false
                playerShipData.input.isReversing = false
            }

            if(axes.axes[2] > 0.5) {
                playerShipData.input.isYawingRight = true
            } else if(axes.axes[2] < -0.5) {
                playerShipData.input.isYawingLeft = true
            } else {
                playerShipData.input.isYawingRight = false
                playerShipData.input.isYawingLeft = false
            }
        }

        if(buttons != null) {
            playerShipData.input.isFiring = buttons.buttons[3]
            playerShipData.input.isPlacingPowerNode = buttons.buttons[1]
            playerShipData.input.isBoosting = buttons.buttons[2]
        }
    }
}

fun SceneBuilder.playerInputComponent() {
    component(Id.COMPONENT_PLAYER_INPUT) {
        PlayerInputComponent()
    }
}