package com.fluxtah.application.api.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    fun lerp(target: Quaternion, alpha: Float): Quaternion {
        // Ensure alpha is between 0 and 1
        val clampedAlpha = alpha.coerceIn(0f, 1f)
        // Linearly interpolate each component
        val interpolatedW = (1 - clampedAlpha) * this.w + clampedAlpha * target.w
        val interpolatedX = (1 - clampedAlpha) * this.x + clampedAlpha * target.x
        val interpolatedY = (1 - clampedAlpha) * this.y + clampedAlpha * target.y
        val interpolatedZ = (1 - clampedAlpha) * this.z + clampedAlpha * target.z
        val result = Quaternion(interpolatedW, interpolatedX, interpolatedY, interpolatedZ)
        // Normalize the result to ensure it's a valid quaternion
        result.normalize()
        return result
    }


    fun getLocalForwardAxis(): Vector3 {
        val globalForward = Vector3(0f, 0f, 1f) // Standard forward vector in global coordinates
        return rotateVector(globalForward)
    }

    fun getLocalUpAxis(): Vector3 {
        val globalUp = Vector3(0f, 1f, 0f) // Standard up vector in global coordinates
        return rotateVector(globalUp)
    }

    fun getLocalRightAxis(): Vector3 {
        val globalRight = Vector3(1f, 0f, 0f) // Standard right vector in global coordinates
        return rotateVector(globalRight)
    }
}