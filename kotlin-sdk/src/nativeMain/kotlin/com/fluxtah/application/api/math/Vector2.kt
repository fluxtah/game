package com.fluxtah.application.api.math

data class Vector2(var x: Float = 0f, var y: Float = 0f) {
    companion object {
        val zero = Vector2(0f, 0f)
    }

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun times(value: Float) = Vector2(x * value, y * value)
    operator fun unaryMinus() = Vector2(-x, -y)

    fun update(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}