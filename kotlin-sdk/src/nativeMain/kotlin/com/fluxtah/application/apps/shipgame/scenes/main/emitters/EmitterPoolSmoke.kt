package com.fluxtah.application.apps.shipgame.scenes.main.emitters

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.emitterPoolSmoke() {
    emitterPool(Id.EMITTER_SMOKE) {
        initialSize(10)
        maxParticles(20)
        particleBatchSize(1)
        texture("textures/particle-smoke.png")
        computeShader("shaders/particle2.comp.spv")
        vertexShader("shaders/particle2.vert.spv")
        fragmentShader("shaders/particle2.frag.spv")

        particleLifetime(0.8f)
        particleSpawnRate(0.1f)
        particleGravity(y = 10f)

        particleSpawnPosition(fromX = -0.5f, toX = 0.5f, fromY = 0f, toY = 0f, fromZ = -0.5f, toZ = 0.5f)
        particleSpawnVelocity(fromX = -10f, toX = 10f, fromY = 40f, toY = 80f, fromZ = -10f, toZ = 10f)
    }
}