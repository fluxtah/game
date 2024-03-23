package com.fluxtah.application.apps.shipgame.scenes

import com.fluxtah.application.api.Application
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.input.Key
import com.fluxtah.application.api.isKeyPressed
import com.fluxtah.application.api.math.Vector3
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.api.scene.scene
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.handleCameraInput
import com.fluxtah.application.apps.shipgame.handleInput
import com.fluxtah.application.apps.shipgame.scenes.main.lightOne
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CollisionGroups {
    // Groups
    const val GROUP_CUBE = 1 shl 0
    const val GROUP_FLOOR = 1 shl 1
    const val GROUP_SHIP = 1 shl 2

    // Masks
    const val MASK_CUBE = GROUP_CUBE or GROUP_FLOOR or GROUP_SHIP
    const val MASK_FLOOR = GROUP_CUBE or GROUP_SHIP
    const val MASK_SHIP = GROUP_CUBE or GROUP_FLOOR
}

fun Application.testScene() {
    scene("test") {
        camera(Id.CAMERA1) {
            position(0.0f, 1.0f, -40.0f)
            fieldOfView(60.0f)
            farPlane(1000.0f)
        }
        lightOne()

        entityPool("cube", modelPath = "models/cube.glb") {
            mass(100f)
            startActive()
            initialSize(1)
            collisionGroup(CollisionGroups.GROUP_CUBE)
            collisionMask(CollisionGroups.MASK_CUBE)
            position(0f, 10f, 0f)
            scale(1f, 1f, 1f)
        }
        entity("plane", modelPath = "models/plane.glb") {
            mass(0f)
            collisionGroup(CollisionGroups.GROUP_FLOOR)
            collisionMask(CollisionGroups.MASK_FLOOR)
            position(0f, -5f, 0f)
            scale(10f, 1f, 10f)
        }

        entity("ship", modelPath = "models/ship/ship.glb") {
            mass(0f)
            kinematic(true)
            collisionGroup(CollisionGroups.GROUP_SHIP)
            collisionMask(CollisionGroups.MASK_SHIP)
            position(0f, 10f, 0f)
            scale(1f, 1f, 1f)
        }
        onSceneCreated {
            it.setActiveCamera(Id.CAMERA1)
        }

        onBeforeSceneUpdate { scene, time, delta ->
            handleInput(scene)
            handleCameraInput(scene, fixedTimeStep)

            if (isKeyPressed(Key.P)) {
                scene.spawnEntityFromPool("cube")
            }
            val ship = scene.entityById("ship")

            if (isKeyPressed(Key.LeftBracket)) {
                val currentRot = ship.getOrientation()
                currentRot.rotateAroundAxis(Vector3.up, 1f)
                ship.setRotation(currentRot.w, currentRot.x, currentRot.y, currentRot.z)
            }
            if (isKeyPressed(Key.RightBracket)) {
                val currentRot = ship.getOrientation()
                currentRot.rotateAroundAxis(Vector3.up, -1f)
                ship.setRotation(currentRot.w, currentRot.x, currentRot.y, currentRot.z)
            }

            if (isKeyPressed(Key.Minus)) {
                val currentRot = ship.getOrientation()
                val axis = currentRot.getLocalForwardAxis()
                currentRot.rotateAroundAxis(axis, 1f)
                ship.setRotation(currentRot.w, currentRot.x, currentRot.y, currentRot.z)
            }

            if (isKeyPressed(Key.Equal)) {
                val currentRot = ship.getOrientation()
                val axis = currentRot.getLocalForwardAxis()
                currentRot.rotateAroundAxis(axis, -1f)
                ship.setRotation(currentRot.w, currentRot.x, currentRot.y, currentRot.z)
            }
        }
    }
}
data class Quaternion(var w: Float, var x: Float, var y: Float, var z: Float) {

    fun normalize() {
        val norm = sqrt((w * w + x * x + y * y + z * z).toDouble()).toFloat()
        w /= norm
        x /= norm
        y /= norm
        z /= norm
    }

    fun multiply(other: Quaternion): Quaternion {
        return Quaternion(
            w * other.w - x * other.x - y * other.y - z * other.z,
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w
        ).also {
            it.normalize()
        }
    }

    fun rotateAroundAxis(axis: Vector3, angleDegrees: Float) {
        // Convert angle from degrees to radians
        val angleRadians = angleDegrees.toRadians() / 2f
        // Calculate the components of the rotation quaternion
        val sinAngle = sin(angleRadians.toDouble()).toFloat()
        val cosAngle = cos(angleRadians.toDouble()).toFloat()
        val rotationQuaternion = Quaternion(
            cosAngle,
            axis.x * sinAngle,
            axis.y * sinAngle,
            axis.z * sinAngle
        )
        // Apply the rotation
        val result = rotationQuaternion.multiply(this)
        w = result.w
        x = result.x
        y = result.y
        z = result.z
    }

    fun rotateVector(v: Vector3): Vector3 {
        val qVec = Vector3(x, y, z)
        val uv = qVec.cross(v)
        val uuv = qVec.cross(uv)
        return v + uv * (2.0f * w) + uuv * 2.0f
    }

    fun getLocalForwardAxis(): Vector3 {
        val globalForward = Vector3(0f, 0f, 1f) // Standard forward vector in global coordinates
        return rotateVector(globalForward)
    }

    fun getLocalUpAxis(quaternion: Quaternion): Vector3 {
        val globalUp = Vector3(0f, 1f, 0f) // Standard up vector in global coordinates
        return quaternion.rotateVector(globalUp)
    }
}

