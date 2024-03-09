package com.fluxtah.application.apps.shipgame.scenes.main

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.KillerCameraBehavior

fun SceneBuilder.killerCamera() {
    camera(Id.KILLER_CAMERA) {
        position(0.0f, 5.0f, 0.0f)
        fieldOfView(60.0f)
        nearPlane(0.1f)
        farPlane(1000.0f)
        behavior { KillerCameraBehavior() }
    }
}