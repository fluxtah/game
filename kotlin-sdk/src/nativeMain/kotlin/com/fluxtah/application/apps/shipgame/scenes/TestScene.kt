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

fun Application.testScene() {
    scene("test") {
        camera(Id.CAMERA1) {
            position(0.0f, 1.0f, -40.0f)
            fieldOfView(60.0f)
            farPlane(1000.0f)
        }
        lightOne()
        emitter("testEmitter") {
            maxParticles(100)
            particleBatchSize(16)
            texture("textures/particle-smoke.png")
            computeShader("shaders/particle2.comp.spv")
            vertexShader("shaders/particle2.vert.spv")
            fragmentShader("shaders/particle2.frag.spv")

            particleLifetime(0.8f)
            particleSpawnRate(0.1f)
            particleGravity(y = 10f)

            particleSpawnPosition(fromX = -0.5f, toX = 0.5f, fromY = 0f, toY = 0f, fromZ = -0.5f, toZ = 0.5f)
            particleSpawnVelocity(fromX = -1f, toX = 1f, fromY = 5f, toY = 8f, fromZ = -1f, toZ = 1f)
        }
        emitter("testEmitter2") {
            maxParticles(100)
            particleBatchSize(16)
            model("models/quad-explosion.glb")
            computeShader("shaders/particle2.comp.spv")
            vertexShader("shaders/particle2.vert.spv")
            fragmentShader("shaders/particle2.frag.spv")

            particleLifetime(1.6f)
            particleSpawnRate(0.0f)
            particleGravity(y = -5.81f)

            particleSpawnPosition(fromX = -0.5f, toX = 0.5f, fromY = 0f, toY = 0f, fromZ = -0.5f, toZ = 0.5f)
            particleSpawnVelocity(fromX = -1f, toX = 1f, fromY = 1f, toY = 2f, fromZ = -1f, toZ = 1f)
        }
        entity("testEntity", modelPath = "models/cube.glb") {
            position(60f, 0f, 0f)
            rotation(0f, 0f, 0f)
            scale(1f, 1f, 1f)
        }
        entity("plane", modelPath = "models/plane.glb") {
            position(0f, -5f, 0f)
            rotation(0f, 0f, 0f)
            scale(10f, 1f, 10f)
        }

        entity("ring", "models/energy-ring/energy-ring.glb") {
            position(0f, -3.7f, 0f)
            scale(10f, 10f, 10f)
            skin(1)
        }

        //            entity("testEntity", modelPath = "models/block-aabb-test.glb") {
        //                position(0f, 0f, 5f)
        //                rotation(0f, 0f, 0f)
        //                scale(1f, 1f, 1f)
        //            }

        onSceneCreated {
            it.setActiveCamera(Id.CAMERA1)
        }

        onBeforeSceneUpdate { scene, time, delta ->
            handleInput(scene)
            handleCameraInput(scene, fixedTimeStep)

            val emitter = scene.emitterById("testEmitter")
            val entity = scene.entityById("testEntity")

            //  emitter!!.setPosition(x = 1 + 20f * sin(time * 0.5f), y = 0f, z = 0f)
            //    entity.setPosition(x = 1f + 20f * sin(time * 0.5f), y = 0f, z = 0f)

            if (isKeyPressed(Key.F4)) {
                emitter!!.setPosition(x = 20f)
                entity.setPosition(x = 20f)
            }
            if (isKeyPressed(Key.F5)) {
                emitter!!.setPosition(x = -20f)
                entity.setPosition(x = -20f)
            }

            val scaleFrom = 1.0f
            val scaleTo = 100.0f
            // calcualte scale based on time between scaleFrom and scaleTo
            val scale = scaleFrom + (sin(time) * (scaleTo - scaleFrom))
            val rotate = time * 0.05f
            val ring = scene.entityById("ring")
            ring.setScale(scale, 1f, scale)
            ring.rotate(y = rotate)
        }
    }
}