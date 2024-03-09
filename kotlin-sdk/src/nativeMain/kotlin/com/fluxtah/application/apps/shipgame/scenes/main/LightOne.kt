package com.fluxtah.application.apps.shipgame.scenes.main

import com.fluxtah.application.api.LightType
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.lightOne() {
    light(Id.LIGHT1) {
        type(LightType.Directional)
        color(1.0f, 1.0f, 1.0f, 1.0f)
        position(0.0f, 200.0f, 0.0f)
        val lightDir = Vector3(0.3f, -0.7f, -0.2f).apply { normalize() }
        direction(lightDir.x, lightDir.y, lightDir.z);
        intensity(0.5f)
    }
}