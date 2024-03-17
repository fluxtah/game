package com.fluxtah.application.apps.shipgame.scenes

import com.fluxtah.application.api.Application
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.input.Key
import com.fluxtah.application.api.isKeyPressed
import com.fluxtah.application.api.scene.scene
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.handleCameraInput
import com.fluxtah.application.apps.shipgame.handleInput
import com.fluxtah.application.apps.shipgame.scenes.main.lightOne
import kotlin.math.sin

object CollisionGroups {
    // Groups
    const val GROUP_CUBE = 1 shl 0
    const val GROUP_FLOOR = 1 shl 1

    // Masks
    const val MASK_CUBE = GROUP_CUBE or GROUP_FLOOR
    const val MASK_FLOOR = GROUP_CUBE
}

fun Application.testScene() {
    scene("test") {
        camera(Id.CAMERA1) {
            position(0.0f, 1.0f, -40.0f)
            fieldOfView(60.0f)
            farPlane(1000.0f)
        }
        lightOne()

        entityPool("cube", modelPath = "models/cube.glb") {
            mass(100f)
            startActive()
            initialSize(1)
            collisionGroup(CollisionGroups.GROUP_CUBE)
            collisionMask(CollisionGroups.MASK_CUBE)
            position(00f, 10f, 0f)
            rotation(0f, 0f, 0f)
            scale(1f, 1f, 1f)
        }
        entity("plane", modelPath = "models/plane.glb") {
            mass(0f)
            collisionGroup(CollisionGroups.GROUP_FLOOR)
            collisionMask(CollisionGroups.MASK_FLOOR)
            position(0f, -5f, 0f)
            rotation(0f, 0f, 0f)
            scale(10f, 1f, 10f)
        }

        onSceneCreated {
            it.setActiveCamera(Id.CAMERA1)
        }

        onBeforeSceneUpdate { scene, time, delta ->
            handleInput(scene)
            handleCameraInput(scene, fixedTimeStep)

            if (isKeyPressed(Key.P)) {
               scene.spawnEntityFromPool("cube")
            }
            if(isKeyPressed(Key.LeftBracket)) {
                scene.entityById("plane").rotate(0f, 0f, 5f)
            }
            if(isKeyPressed(Key.RightBracket)) {
                scene.entityById("plane").rotate(0f, 0f, -5f)
            }
        }
    }
}