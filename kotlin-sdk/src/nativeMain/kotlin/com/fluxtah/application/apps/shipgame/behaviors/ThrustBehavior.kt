package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.Sound
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.components.map.TileType
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class ThrustBehavior : EntityBehavior() {
    val state = ThrustBehaviorState()
    val shipData: ShipData by lazy { entity.data() }

    private lateinit var thrustSound: Sound
    private lateinit var map: MapComponent

    override fun initialize() {
        thrustSound = scene.soundFromPool(Id.SOUND_UP_THRUST)
        map = scene.componentById(Id.COMPONENT_MAP)
        state.positionY = 8.0f
    }

    override fun reset() {
        if (!::thrustSound.isInitialized) return

        thrustSound.stopIfPlaying()
        state.reset()
        state.positionY = 8.0f
    }

    override fun beforeUpdate(time: Float, deltaTime: Float) {
        state.thrusting = shipData.input.isThrusting
        if (state.thrusting) {
            if (!thrustSound.isPlaying()) {
                scene.soundFromPool(Id.SOUND_SONIC_BOOM).play()
                thrustSound.play()
            }
        } else {
            thrustSound.stopIfPlaying()
        }

        map.getTileByWorldPosition(entity.positionX, entity.positionZ).apply {
            if (type == TileType.LEVEL6) {
                state.groundLevel = 8.0f
            } else {
                state.groundLevel = 0.0f
            }
        }
    }

    override fun update(time: Float) {
        // Adjust thrust based on key input
        if (state.thrusting) {
            thrustSound.setSoundPosition(entity.positionX, entity.positionY, entity.positionZ)
            state.thrust = (state.thrust + state.thrustIncrement * fixedTimeStep).coerceAtMost(state.maxThrust)
        } else if (state.thrust > 0.0f) {
            state.thrust = (state.thrust - state.thrustIncrement * fixedTimeStep).coerceAtLeast(0.0f)
        }

        // Calculate forces and acceleration
        val gravitationalForce = state.shipMass * state.gravity
        val netForce = state.thrust - gravitationalForce
        val acceleration = netForce / state.shipMass

        // Update velocity and position
        state.velocityY += acceleration * fixedTimeStep
        state.positionY += state.velocityY * fixedTimeStep

        // Apply max velocity
        state.velocityY = state.velocityY.coerceAtMost(state.maxVelocity)

        // When at ground level and not thrusting
        if (state.positionY <= state.groundLevel && !state.thrusting) {
            state.velocityY = 0.0f
        }

        // Apply damping if overshooting max altitude
        if (state.positionY > state.maxAltitude) {
            state.velocityY *= state.overshootDamping
            state.positionY = state.maxAltitude + (state.positionY - state.maxAltitude) * state.overshootDamping
        }

        // Apply hover damping for smooth hovering
        if (state.positionY >= state.maxAltitude && state.thrust > gravitationalForce) {
            state.thrust = gravitationalForce
            state.velocityY *= state.hoverDamping
        }

        // Ensure the spaceship doesn't go below the ground
        state.positionY = state.positionY.coerceAtLeast(state.groundLevel)

        entity.setPosition(y = state.positionY)
    }

//    override fun afterUpdate(scene: Scene, entity: Entity, time: Float, deltaTime: Float) {
//        println("\rState: $state")
//    }

    data class ThrustBehaviorState(
        var groundLevel: Float = 0.0f,
        var velocityY: Float = 0.0f,
        var positionY: Float = 0.0f,
        val gravity: Float = 4.0f,
        val shipMass: Float = 200.0f,
        var thrust: Float = 0.0f,
        val maxThrust: Float = 30000.0f,
        val maxAltitude: Float = 12.2f,
        val thrustIncrement: Float = 9000f,
        val overshootDamping: Float = 0.95f,
        val hoverDamping: Float = 0.99f,
        val maxVelocity: Float = 100.0f,
        var thrusting: Boolean = false,
    ) {
        fun reset() {
            velocityY = 0.0f
            positionY = 0.0f
            thrust = 0.0f
            thrusting = false
        }
    }
}