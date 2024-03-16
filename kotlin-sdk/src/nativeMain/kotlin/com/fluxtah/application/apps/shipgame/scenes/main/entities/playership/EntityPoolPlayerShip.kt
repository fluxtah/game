package com.fluxtah.application.apps.shipgame.scenes.main.entities.playership

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame
import com.fluxtah.application.apps.shipgame.behaviors.FirePlasmaCannonBehaviour
import com.fluxtah.application.apps.shipgame.behaviors.ShipMovementBehavior
import com.fluxtah.application.apps.shipgame.behaviors.PlacePowerNodeBehavior
import com.fluxtah.application.apps.shipgame.behaviors.ShipCollisionResponseBehavior
import com.fluxtah.application.apps.shipgame.behaviors.ShipDieBehavior
import com.fluxtah.application.apps.shipgame.behaviors.ShipEnergyBehaviour
import com.fluxtah.application.apps.shipgame.behaviors.ShipShieldBehaviour
import com.fluxtah.application.apps.shipgame.behaviors.ThrustBehavior
import com.fluxtah.application.apps.shipgame.behaviors.YawBehavior
import com.fluxtah.application.apps.shipgame.behaviors.aiplayer.AiPlayerShipBehavior
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

fun SceneBuilder.entityPoolPlayerShip() {
    entityPool(Id.ENT_PLAYER_SHIP, "models/ship/ship.glb") {
        mass(200f)
        kinematic(true)
        collisionGroup(CollisionGroups.GROUP_PLAYER)
        collisionMask(CollisionGroups.MASK_PLAYER)
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2)
        data { ShipData() }
        behaviour { AiPlayerShipBehavior() }
        behaviour { FirePlasmaCannonBehaviour() }
        behaviour { ThrustBehavior() }
        behaviour { YawBehavior() }
        behaviour { ShipMovementBehavior() }
        behaviour { ShipDieBehavior() }
        behaviour { ShipCollisionResponseBehavior() }
        behaviour { ShipEnergyBehaviour() }
        behaviour { ShipShieldBehaviour() }
        behaviour { PlacePowerNodeBehavior() }
    }
}
