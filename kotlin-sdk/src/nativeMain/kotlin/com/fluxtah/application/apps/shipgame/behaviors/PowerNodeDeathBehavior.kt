package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.Id

class PowerNodeDeathBehavior : EntityBehavior() {
    fun die() {
        if (entity.active) {
            entity.getBehaviorByType<PowerNodeSmokeBehavior>().apply {
                smokeEmitter?.let { scene.emitterToPool(it) }
                smokeEmitter = null
            }
            scene.entityToPool(entity)

            scene.soundFromPool(Id.SOUND_ASTEROID_EXPLODE).apply {
                setSoundPosition(entity.positionX, entity.positionY, entity.positionZ)
                play()
            }
            entity.visible = false
            scene.emitterFromPool(Id.EMITTER_EXPLOSION) {
                it.setPosition(entity.positionX, entity.positionY, entity.positionZ)
                val behavior = it.getBehaviorByType<ExplosionEmitterBehavior>()
                behavior.explode()
            }
        }
    }
}
