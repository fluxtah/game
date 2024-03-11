package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import com.fluxtah.application.apps.shipgame.scenes.main.sequences.SequencePlayerDeathRespawnArgs

class ShipDieBehavior : EntityBehavior() {
    val data: ShipData by lazy { entity.data() }

    fun die(killer: Entity) {
        if (entity.active) {
            data.playerData.deaths++
            scene.soundFromPool(Id.SOUND_ASTEROID_EXPLODE).apply {
                setSoundPosition(entity.positionX, entity.positionY, entity.positionZ)
                play()
            }
            entity.visible = false
            entity.active = false
            entity.getBehaviorByType<ShipMovementBehavior>().stopEngine()
            scene.emitterFromPool(Id.EMITTER_EXPLOSION) {
                it.setPosition(entity.positionX, entity.positionY, entity.positionZ)
                val behavior = it.getBehaviorByType<ExplosionEmitterBehavior>()
                behavior.explode()
            }
            scene.createSequence(Id.SEQ_DEATH_RESPAWN)!!.play(
                SequencePlayerDeathRespawnArgs(
                    killer = killer,
                    ship = entity
                )
            )
        }
    }
}
