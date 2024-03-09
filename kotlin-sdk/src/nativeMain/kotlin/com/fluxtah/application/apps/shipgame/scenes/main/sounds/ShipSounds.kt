package com.fluxtah.application.apps.shipgame.scenes.main.sounds

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame

fun SceneBuilder.shipSounds() {
    //
    // Ship Sounds
    //
    soundPool(Id.SOUND_UP_THRUST, "sounds/up-thrust.wav") {
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2)
        loop(true)
    }
    soundPool(Id.SOUND_ENGINE, "sounds/engine.wav") {
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2)
        loop(true)
    }
    soundPool(Id.SOUND_LATERAL_THRUST, "sounds/lateral-thrust.wav") {
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2)
        loop(true)
    }
    soundPool(Id.SOUND_SONIC_BOOM, "sounds/sonic-boom.wav") {
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2)
    }
    soundPool(Id.SOUND_PLASMA_BOLT, "sounds/plasma-bolt.wav") {
        initialSize(ShipGame.PLAYERS_PER_TEAM * 2 * 20)
    }

    soundPool(Id.SOUND_ASTEROID_EXPLODE, "sounds/asteroid-explode.wav")
}