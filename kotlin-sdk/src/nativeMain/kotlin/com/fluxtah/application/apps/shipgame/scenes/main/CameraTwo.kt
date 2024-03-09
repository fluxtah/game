package com.fluxtah.application.apps.shipgame.scenes.main

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.cameraTwo() {
    camera(Id.CAMERA2) {
        position(4.0f, 5.0f, -4.0f)
    }
}

