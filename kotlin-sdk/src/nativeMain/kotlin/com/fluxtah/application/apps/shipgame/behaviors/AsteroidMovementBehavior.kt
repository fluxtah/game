package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import kotlin.random.Random

class AsteroidMovementBehavior(
    private val speedX: Float = 0.0f,
    private val speedY: Float = 0.0f,
    private val speedZ: Float = 0.0f
) : EntityBehavior() {

    override fun initialize() {
        entity.setPosition(-400 + (Random.nextFloat() * 400), 100f, -400 + (Random.nextFloat() * 400))
        entity.setScale(
            x = 0.9f + (Random.nextFloat() * 1.2f),
            y = 0.9f + (Random.nextFloat() * 1.2f),
            z = 0.9f + (Random.nextFloat() * 1.2f)
        )
    }

    override fun update(time: Float) {
        entity.setRotation(
            entity.rotationX + (fixedTimeStep * speedX),
            entity.rotationY + (fixedTimeStep * speedY),
            entity.rotationZ + (fixedTimeStep * speedZ)
        )
    }
}