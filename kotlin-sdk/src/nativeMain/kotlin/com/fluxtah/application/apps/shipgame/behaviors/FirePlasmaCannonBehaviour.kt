package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PlasmaBoltData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class FirePlasmaCannonBehaviour(
    private val plasmaBoltEntityId: String = Id.ENT_PLAYER_PLASMA_BOLT
) : EntityBehavior() {
    private val shipData: ShipData by lazy { entity.data() }
    private val sceneData: GameData by lazy { scene.data() }

    private var lastFireTime = 0.0f
    override fun update(time: Float) {
        val fireDelayPenalty = if(shipData.energy < shipData.plasmaCannonEnergyCost) {
            shipData.plasmaCannonFireDelay * sceneData.shipNoEnergyFirePenaltyFactor
        } else {
            0f
        }
        val canFireBolt = (time - lastFireTime > shipData.plasmaCannonFireDelay + fireDelayPenalty)

        if (shipData.input.isFiring && canFireBolt) {
            lastFireTime = time
            scene.entityFromPool(plasmaBoltEntityId) { bolt ->

                val boltData = bolt.data<PlasmaBoltData>()
                val shipData = entity.data<ShipData>()

                boltData.team = shipData.playerData.team
                boltData.owner = entity

                shipData.depleteEnergy(shipData.plasmaCannonEnergyCost)

                val boltBehaviour = bolt.getBehaviorByType<PlasmaBoltBehaviour>()
                bolt.setPosition(entity.positionX, entity.positionY, entity.positionZ)
                bolt.setRotation(entity.rotationX, entity.rotationY, entity.rotationZ)
                bolt.visible = true
                boltBehaviour.initialPosition.x = entity.positionX
                boltBehaviour.initialPosition.y = entity.positionY
                boltBehaviour.initialPosition.z = entity.positionZ
                boltBehaviour.firingDirection = Vector3.calculateDirectionFromYaw(entity.rotationY)
                boltBehaviour.fireBolt()
            }
        }
    }
}
