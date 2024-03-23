package com.fluxtah.application.api.math

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    companion object {
        val zero = Vector3(0f, 0f, 0f)

        val up = Vector3(0f, 1f, 0f)

        // Linear interpolation (lerp) function
        fun lerp(a: Vector3, b: Vector3, t: Float): Vector3 = Vector3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        )

        fun distanceBetween(start: Vector3, end: Vector3): Float = sqrt(
            (end.x - start.x).pow(2) +
                    (end.y - start.y).pow(2) +
                    (end.z - start.z).pow(2)
        )

        fun distanceBetween(
            startX: Float,
            startY: Float,
            startZ: Float,
            endX: Float,
            endY: Float,
            endZ: Float
        ): Float = sqrt(
            (endX - startX).pow(2) +
                    (endY - startY).pow(2) +
                    (endZ - startZ).pow(2)
        )

        fun calculateDirectionFromYaw(rotationY: Float): Vector3 {
            // Convert rotationY (yaw) to radians
            val radians = rotationY.toRadians()

            // Calculate direction vector using yaw
            val dirX = sin(radians)
            val dirZ = cos(radians)

            return Vector3(dirX, 0.0f, dirZ)
        }
    }

    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun times(value: Float) = Vector3(x * value, y * value, z * value)
    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun update(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun normalize() {
        val length = sqrt(x * x + y * y + z * z)
        x /= length
        y /= length
        z /= length
    }

    // Calculate and return the cross product of this vector with another vector
    fun cross(other: Vector3): Vector3 {
        return Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }
}

fun Vector3.lerp(target: Vector3, alpha: Float) {
    x = x * (1 - alpha) + target.x * alpha
    y = y * (1 - alpha) + target.y * alpha
    z = z * (1 - alpha) + target.z * alpha
}

fun Vector3.distanceTo(end: Vector3): Float = sqrt(
    (end.x - x).pow(2) +
            (end.y - y).pow(2) +
            (end.z - z).pow(2)
)

fun Vector3.length(): Float = sqrt(x * x + y * y + z * z)

