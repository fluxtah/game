package com.fluxtah.application.apps.shipgame.scenes.main

import com.fluxtah.application.api.LightType
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.lightTwo() {
    light(Id.LIGHT2) {
        type(LightType.Point)
        color(1.0f, 1.0f, 1.0f, 1.0f)
        position(0.0f, 200f, 0.0f)
        val lightDir = Vector3(-0.3f, -0.7f, 0.2f).apply { normalize() }
        direction(lightDir.x, lightDir.y, lightDir.z);
        intensity(20.0f)
    }
}