package com.fluxtah.application.apps.shipgame

import com.fluxtah.application.api.enableDebugBoundingVolumes
import com.fluxtah.application.api.input.Key
import com.fluxtah.application.api.isKeyPressed
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData

fun handleInput(scene: Scene) {
    if (isKeyPressed(Key.Num1)) {
        scene.setActiveCamera(Id.CAMERA1)
    }
    if (isKeyPressed(Key.Num2)) {
        scene.setActiveCamera(Id.CAMERA2)
    }
    if (isKeyPressed(Key.F1)) {
        enableDebugBoundingVolumes(true)
    }
    if (isKeyPressed(Key.F2)) {
        enableDebugBoundingVolumes(false)
    }
    val data = scene.data<GameData>()

    if (isKeyPressed(Key.Num0)) {
        data.aiShipsEnabled = !data.aiShipsEnabled
    }
}