package com.fluxtah.application.api.emitter

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.file.toAssetsPath
import com.fluxtah.application.api.interop.c_createEmitter
import com.fluxtah.application.api.interop.model.CreateEmitterInfo
import com.fluxtah.application.api.scene.EmitterInfo
import com.fluxtah.application.api.scene.EmitterPoolInfo
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.SceneDsl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped

@SceneDsl
@OptIn(ExperimentalForeignApi::class)
class EmitterPoolBuilder(private val scene: Scene, private val id: String) {
    private var maxParticles: Int = 100
    private var particleBatchSize: Int = 4

    private var positionX: Float = 0.0f
    private var positionY: Float = 0.0f
    private var positionZ: Float = 0.0f
    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var rotationZ: Float = 0.0f
    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f
    private var scaleZ: Float = 1.0f

    private var particleLifetime: Float = 1.0f
    private var particleSpawnRate: Float = 1.0f

    private var particleGravityX: Float = 0.0f
    private var particleGravityY: Float = -9.81f
    private var particleGravityZ: Float = 0.0f

    private var particleSpawnPositionFromX: Float = 0.0f
    private var particleSpawnPositionFromY: Float = 0.0f
    private var particleSpawnPositionFromZ: Float = 0.0f
    private var particleSpawnPositionToX: Float = 0.0f
    private var particleSpawnPositionToY: Float = 0.0f
    private var particleSpawnPositionToZ: Float = 0.0f

    private var particleSpawnVelocityFromX: Float = 0.0f
    private var particleSpawnVelocityFromY: Float = 0.0f
    private var particleSpawnVelocityFromZ: Float = 0.0f
    private var particleSpawnVelocityToX: Float = 0.0f
    private var particleSpawnVelocityToY: Float = 10.0f
    private var particleSpawnVelocityToZ: Float = 0.0f

    private var computeShaderPath: String? = null
    private var vertexShaderPath: String? = null
    private var fragmentShaderPath: String? = null
    private var modelPath: String? = null
    private var texturePath: String? = null

    private var initialSize: Int = 10
    private var startActive: Boolean = false

    private val behaviors = mutableListOf<() -> EmitterBehavior>()

    fun model(path: String) {
        modelPath = path
    }

    fun texture(path: String) {
        texturePath = path
    }

    fun initialSize(size: Int) {
        initialSize = size
    }

    fun startActive() {
        startActive = true
    }

    fun maxParticles(size: Int) {
        maxParticles = size
    }

    fun particleBatchSize(size: Int) {
        particleBatchSize = size
    }

    fun position(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        positionX = x
        positionY = y
        positionZ = z
    }

    fun rotation(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        rotationX = x
        rotationY = y
        rotationZ = z
    }

    fun scale(x: Float = 1f, y: Float = 1f, z: Float = 1f) {
        scaleX = x
        scaleY = y
        scaleZ = z
    }

    fun particleLifetime(lifetime: Float) {
        particleLifetime = lifetime
    }

    fun particleSpawnRate(rate: Float) {
        particleSpawnRate = rate
    }

    fun particleGravity(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        particleGravityX = x
        particleGravityY = y
        particleGravityZ = z
    }

    fun particleSpawnPosition(fromX: Float = 0f, fromY: Float = 0f, fromZ: Float = 0f, toX: Float = 0f, toY: Float = 0f, toZ: Float = 0f) {
        particleSpawnPositionFromX = fromX
        particleSpawnPositionFromY = fromY
        particleSpawnPositionFromZ = fromZ
        particleSpawnPositionToX = toX
        particleSpawnPositionToY = toY
        particleSpawnPositionToZ = toZ
    }

    fun particleSpawnPosition(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        particleSpawnPositionFromX = x
        particleSpawnPositionFromY = y
        particleSpawnPositionFromZ = z
        particleSpawnPositionToX = x
        particleSpawnPositionToY = y
        particleSpawnPositionToZ = z
    }

    fun particleSpawnVelocity(fromX: Float = 0f, fromY: Float = 0f, fromZ: Float = 0f, toX: Float = 0f, toY: Float = 0f, toZ: Float = 0f) {
        particleSpawnVelocityFromX = fromX
        particleSpawnVelocityFromY = fromY
        particleSpawnVelocityFromZ = fromZ
        particleSpawnVelocityToX = toX
        particleSpawnVelocityToY = toY
        particleSpawnVelocityToZ = toZ
    }

    fun particleSpawnVelocity(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        particleSpawnVelocityFromX = x
        particleSpawnVelocityFromY = y
        particleSpawnVelocityFromZ = z
        particleSpawnVelocityToX = x
        particleSpawnVelocityToY = y
        particleSpawnVelocityToZ = z
    }

    fun computeShader(path: String) {
        computeShaderPath = path
    }

    fun vertexShader(path: String) {
        vertexShaderPath = path
    }

    fun fragmentShader(path: String) {
        fragmentShaderPath = path
    }

    fun behaviour(behavior: () -> EmitterBehavior) {
        behaviors.add(behavior)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun build(): EmitterPoolInfo {
        val initialEmitters = mutableListOf<EmitterInfo>().apply {
            repeat(initialSize) {
                add(createEmitterInfo())
            }
        }
        return EmitterPoolInfo(
            initialSize = initialSize,
            factory = { createEmitterInfo() },
            emittersAvailable = if (startActive) mutableListOf() else initialEmitters,
            emittersInUse = if (startActive) initialEmitters.onEach { it.emitter.inUse = true } else mutableListOf()
        )
    }

    private fun createEmitterInfo(): EmitterInfo {
        val cEmitter = memScoped {
            val info = cValue<CreateEmitterInfo> {
                modelFileName = modelPath?.toAssetsPath()?.cstr?.ptr
                textureFileName = texturePath?.toAssetsPath()?.cstr?.ptr
                computeShaderFileName = computeShaderPath?.toAssetsPath()?.cstr?.ptr
                vertexShaderFileName = vertexShaderPath?.toAssetsPath()?.cstr?.ptr
                fragmentShaderFileName = fragmentShaderPath?.toAssetsPath()?.cstr?.ptr

                maxParticles = this@EmitterPoolBuilder.maxParticles
                particleBatchSize = this@EmitterPoolBuilder.particleBatchSize

                particleLifetime = this@EmitterPoolBuilder.particleLifetime
                particleSpawnRate = this@EmitterPoolBuilder.particleSpawnRate

                particleGravityX = this@EmitterPoolBuilder.particleGravityX
                particleGravityY = this@EmitterPoolBuilder.particleGravityY
                particleGravityZ = this@EmitterPoolBuilder.particleGravityZ

                particleSpawnPositionFromX = this@EmitterPoolBuilder.particleSpawnPositionFromX
                particleSpawnPositionFromY = this@EmitterPoolBuilder.particleSpawnPositionFromY
                particleSpawnPositionFromZ = this@EmitterPoolBuilder.particleSpawnPositionFromZ
                particleSpawnPositionToX = this@EmitterPoolBuilder.particleSpawnPositionToX
                particleSpawnPositionToY = this@EmitterPoolBuilder.particleSpawnPositionToY
                particleSpawnPositionToZ = this@EmitterPoolBuilder.particleSpawnPositionToZ

                particleSpawnVelocityFromX = this@EmitterPoolBuilder.particleSpawnVelocityFromX
                particleSpawnVelocityFromY = this@EmitterPoolBuilder.particleSpawnVelocityFromY
                particleSpawnVelocityFromZ = this@EmitterPoolBuilder.particleSpawnVelocityFromZ
                particleSpawnVelocityToX = this@EmitterPoolBuilder.particleSpawnVelocityToX
                particleSpawnVelocityToY = this@EmitterPoolBuilder.particleSpawnVelocityToY
                particleSpawnVelocityToZ = this@EmitterPoolBuilder.particleSpawnVelocityToZ

                emitterPositionX = this@EmitterPoolBuilder.positionX
                emitterPositionY = this@EmitterPoolBuilder.positionY
                emitterPositionZ = this@EmitterPoolBuilder.positionZ
                emitterRotationX = this@EmitterPoolBuilder.rotationX
                emitterRotationY = this@EmitterPoolBuilder.rotationY
                emitterRotationZ = this@EmitterPoolBuilder.rotationZ
                emitterScaleX = this@EmitterPoolBuilder.scaleX
                emitterScaleY = this@EmitterPoolBuilder.scaleY
                emitterScaleZ = this@EmitterPoolBuilder.scaleZ
            }
            c_createEmitter!!.invoke(ApplicationContext.vulcanContext!!, info.ptr)
        }

        val behaviors = behaviors.map { it().apply { this.scene = this@EmitterPoolBuilder.scene } }

        return EmitterInfo(
            emitter = Emitter(
                id = id,
                handle = cEmitter,
                initialPositionX = positionX,
                initialPositionY = positionY,
                initialPositionZ = positionZ,
                initialRotationX = rotationX,
                initialRotationY = rotationY,
                initialRotationZ = rotationZ,
                initialScaleX = scaleX,
                initialScaleY = scaleY,
                initialScaleZ = scaleZ,
                initialLifetime = particleLifetime,
                initialSpawnRate = particleSpawnRate,
                inUse = false,
                behaviors = behaviors
            ),
            behaviors = behaviors,
        )
    }
}