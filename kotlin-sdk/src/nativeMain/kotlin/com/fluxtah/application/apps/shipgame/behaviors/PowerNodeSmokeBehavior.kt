package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.emitter.Emitter
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import platform.posix.pow
import platform.posix.powf

class PowerNodeSmokeBehavior : EntityBehavior() {
    val data by lazy { entity.data<PowerNodeData>() }

    var smokeEmitter: Emitter? = null

    override fun update(time: Float) {
        if (data.health < 70 && smokeEmitter == null) {
          //  println("Creating smoke emitter")
            scene.emitterFromPool(Id.EMITTER_SMOKE) {
                smokeEmitter = it
                it.setPosition(entity.positionX, entity.positionY + 10f, entity.positionZ)
                it.visible = true
                it.reset()
                it.setParticleSpawnRate(1.2f)
            }
        }

        if (smokeEmitter != null && data.health > 70) {
            if(smokeEmitter!!.inUse) {
                scene.emitterToPool(smokeEmitter!!)
            }
            smokeEmitter = null
        } else if (smokeEmitter != null && data.health > 0) {
          //  println("Updating smoke emitter, rate: ${(data.health / 100f) * 1.2f}")
            smokeEmitter!!.setParticleSpawnRate(powf(data.health / 100f, 2f) * 1.2f)
        }
    }
}