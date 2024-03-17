package com.fluxtah.application.apps.shipgame.scenes.main.entities

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.PlasmaBoltBehaviour
import com.fluxtah.application.apps.shipgame.scenes.main.data.PlasmaBoltData

fun SceneBuilder.entityPoolPlayerPlasmaBolt() {
    entityPool(Id.ENT_PLAYER_PLASMA_BOLT, "models/plasma-bolt.glb") {
        //useOrientedBoundingBox()
        kinematic(true)
        collisionGroup(CollisionGroups.GROUP_PROJECTILE)
        collisionMask(CollisionGroups.MASK_PROJECTILE)
        data { PlasmaBoltData() }
        initialSize(100)
        behaviour {
            PlasmaBoltBehaviour()
        }
    }
}

