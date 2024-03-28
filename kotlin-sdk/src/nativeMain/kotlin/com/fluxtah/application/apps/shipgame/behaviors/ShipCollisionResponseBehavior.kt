package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.CollisionContactPoint
import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.times
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import kotlin.math.absoluteValue

class ShipCollisionResponseBehavior : EntityBehavior() {
    fun handleResponse(otherEntity: Entity, contactPoints: List<CollisionContactPoint>) {
        if (contactPoints.isNotEmpty()) {
            // Assuming we're dealing with the spaceship and a plateau
            val maxPenetration = contactPoints.maxOf { it.distance } // Find the maximum penetration depth
            val normal = contactPoints[0].normal

            // println("Collision depth: $maxPenetration")
            // println("Collision normal: $normal")

            // Calculate the adjustment needed to move the spaceship out of collision
            // You may add a small buffer (e.g., 0.01) to ensure they don't immediately collide again
            val adjustment = (normal) * (maxPenetration.absoluteValue + 0.01f)

            // Adjust the spaceship's position
            val currentPosition = entity.getPosition()
            val newPosition = currentPosition + adjustment
            entity.setPosition(newPosition)

            // Reflect the spaceship's velocity
            val velocity = entity.getVelocity()
            val newVelocity = reflect(velocity, normal)

            if (otherEntity.data is ShipData) {
                entity.setVelocity(newVelocity * 0.95f)

                // If the other entity is a spaceship, reflect its velocity as well
                val otherVelocity = otherEntity.getVelocity()
                val newOtherVelocity = reflect(otherVelocity, normal)
                otherEntity.setVelocity(newOtherVelocity * 0.95f)
            } else {
                entity.setVelocity(newVelocity * 0.5f)
            }
        }
    }

    private fun reflect(velocity: Vector3, normal: Vector3): Vector3 {
        // v - 2 * (v . n) * n
        return velocity - 2 * velocity.dot(normal) * normal
    }

    override fun update(time: Float) {}
}

