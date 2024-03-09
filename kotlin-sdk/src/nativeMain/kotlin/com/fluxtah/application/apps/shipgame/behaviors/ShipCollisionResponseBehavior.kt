package com.fluxtah.application.apps.shipgame.behaviors

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.entity.EntityBehavior
import com.fluxtah.application.api.entity.getEntityCollisionInfo
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData
import platform.posix.printf
import kotlin.math.abs

class ShipCollisionResponseBehavior : EntityBehavior() {
    private val thrustBehavior: ThrustBehavior by lazy { entity.getBehaviorByType() }

    private val shipData: ShipData by lazy { entity.data() }

    fun handleResponse(
        scene: Scene, otherEntity: Entity, sourceEntityAABBIndex: Int, targetEntityAABBIndex: Int
    ) {
        val info = getEntityCollisionInfo(entity, otherEntity, sourceEntityAABBIndex, targetEntityAABBIndex)

      //  println("Collision info: ${info}\n")

        // Adjust position to resolve collision
      //  printf("(BEFORE) Entity to position: %f, %f, %f\n", entity.positionX, entity.positionY, entity.positionZ)
        val collisionCompensation = 1.09f
        entity.setPosition(
            entity.positionX + (info.penetration.x * info.normal.x * collisionCompensation),
            entity.positionY + (info.penetration.y * info.normal.y * collisionCompensation),
            entity.positionZ + (info.penetration.z * info.normal.z * collisionCompensation)
        )

      // printf("(AFTER) Moved Entity to position: %f, %f, %f\n", entity.positionX, entity.positionY, entity.positionZ)


        shipData.velocity.x = 0.0f
        shipData.velocity.z = 0.0f

        if (info.normal.y > 0.0f) {
            thrustBehavior.state.velocityY *= -1.2f
        }
    }

    override fun update(time: Float) {}
}