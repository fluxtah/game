package com.fluxtah.application.apps.shipgame.scenes.main.entities

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.AsteroidDieBehavior
import com.fluxtah.application.apps.shipgame.behaviors.AsteroidMovementBehavior
import kotlin.random.Random

fun SceneBuilder.entityPoolAsteroid() {
    entityPool(Id.ENT_ASTEROID, "models/asteroid.glb") {
        initialSize(10)
        mass(20f)
      //  kinematic(true)
        collisionGroup(CollisionGroups.GROUP_ASTEROID)
        collisionMask(CollisionGroups.MASK_ASTEROID)
        startActive()
        behaviour { AsteroidDieBehavior() }
        behaviour {
            AsteroidMovementBehavior(
                speedX = Random.nextFloat() * 50,
                speedY = Random.nextFloat() * 50,
                speedZ = Random.nextFloat() * 50
            )
        }
    }
}