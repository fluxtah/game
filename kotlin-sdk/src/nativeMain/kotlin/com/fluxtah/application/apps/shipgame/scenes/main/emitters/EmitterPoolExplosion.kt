package com.fluxtah.application.apps.shipgame.scenes.main.emitters

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.ExplosionEmitterBehavior

fun SceneBuilder.emitterPoolExplosion() {
    emitterPool(Id.EMITTER_EXPLOSION) {
        model("models/quad-explosion.glb")
        initialSize(8)
        behaviour { ExplosionEmitterBehavior() }
    }
}