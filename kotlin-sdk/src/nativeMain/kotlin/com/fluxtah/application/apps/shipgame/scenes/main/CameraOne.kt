package com.fluxtah.application.apps.shipgame.scenes.main

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.ChaseCameraBehavior

fun SceneBuilder.cameraOne() {
    camera(Id.CAMERA1) {
        position(4.0f, 6.0f, -4.0f)
        fieldOfView(60.0f)
        farPlane(1000.0f)

        behavior {
            ChaseCameraBehavior()
        }
    }
}