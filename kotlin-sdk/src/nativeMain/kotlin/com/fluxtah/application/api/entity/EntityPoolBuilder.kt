package com.fluxtah.application.api.entity

import com.fluxtah.application.api.*
import com.fluxtah.application.api.file.toAssetsPath
import com.fluxtah.application.api.interop.c_attachKotlinEntity
import com.fluxtah.application.api.interop.c_createEntity
import com.fluxtah.application.api.interop.c_initEntityPhysics
import com.fluxtah.application.api.interop.model.CreateEntityInfo
import com.fluxtah.application.api.scene.*
import kotlinx.cinterop.*

@SceneDsl
@OptIn(ExperimentalForeignApi::class)
class EntityPoolBuilder(private val scene: Scene, private val id: String, private val modelPath: String) {
    private var positionX: Float = 0.0f
    private var positionY: Float = 0.0f
    private var positionZ: Float = 0.0f
    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var rotationZ: Float = 0.0f
    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f
    private var scaleZ: Float = 1.0f
    private var velocityX: Float = 0.0f
    private var velocityY: Float = 0.0f
    private var velocityZ: Float = 0.0f
    private var mass: Float = 1.0f

    private var enablePhysics: Boolean = true
    private var isKinematic: Boolean = false

    private var data: () -> Any = {}

    private var initialSize: Int = 10
    private var startActive: Boolean = false

    private var collisionGroup: Int = 0
    private var collisionMask: Int = 0

    private var skinIndex = 0

    private val behaviors = mutableListOf<() -> EntityBehavior>()

    fun initialSize(size: Int) {
        initialSize = size
    }

    /**
     * If true, the entities will be created and marked as in use
     */
    fun startActive() {
        startActive = true
    }

    fun physics(enabled: Boolean = true) {
        enablePhysics = enabled
    }

    fun kinematic(enabled: Boolean = true) {
        isKinematic = enabled
    }

    fun <T : Any> data(block: () -> T) {
        data = block
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

    fun velocity(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        velocityX = x
        velocityY = y
        velocityZ = z
    }

    fun mass(mass: Float) {
        this.mass = mass
    }

    fun collisionGroup(group: Int) {
        collisionGroup = group
    }

    fun collisionMask(mask: Int) {
        collisionMask = mask
    }

    fun behaviour(behavior: () -> EntityBehavior) {
        behaviors.add(behavior)
    }

    fun skin(index: Int) {
        skinIndex = index
    }

    @OptIn(ExperimentalForeignApi::class)
    fun build(): EntityPoolInfo {
        val initialEntities = mutableListOf<EntityInfo>().apply {
            repeat(initialSize) {
                add(createEntityInfo())
            }
        }
        val entityPool = EntityPool(
            entities = initialEntities.toList(),
            entitiesAvailable = if (startActive) mutableListOf() else initialEntities,
            entitiesInUse = if (startActive) initialEntities.onEach { it.entity.active = true } else mutableListOf()
        )
        return EntityPoolInfo(
            initialSize = initialSize,
            factory = { createEntityInfo() },
            entityPool = entityPool
        )
    }

    private fun createEntityInfo(): EntityInfo {
        val cEntity = memScoped {
            val info = cValue<CreateEntityInfo> {
                modelFileName = modelPath.toAssetsPath().cstr.ptr
                positionX = this@EntityPoolBuilder.positionX
                positionY = this@EntityPoolBuilder.positionY
                positionZ = this@EntityPoolBuilder.positionZ
                rotationX = this@EntityPoolBuilder.rotationX
                rotationY = this@EntityPoolBuilder.rotationY
                rotationZ = this@EntityPoolBuilder.rotationZ
                scaleX = this@EntityPoolBuilder.scaleX
                scaleY = this@EntityPoolBuilder.scaleY
                scaleZ = this@EntityPoolBuilder.scaleZ
                velocityX = this@EntityPoolBuilder.velocityX
                velocityY = this@EntityPoolBuilder.velocityY
                velocityZ = this@EntityPoolBuilder.velocityZ
                mass = this@EntityPoolBuilder.mass
                collisionGroup = this@EntityPoolBuilder.collisionGroup
                collisionMask = this@EntityPoolBuilder.collisionMask
                skinIndex = this@EntityPoolBuilder.skinIndex
            }
            c_createEntity!!.invoke(ApplicationContext.vulcanContext!!, info.ptr)
        }

        val behaviors = behaviors.map { it().apply { this.scene = this@EntityPoolBuilder.scene } }
        val data = data()

        val entity = Entity(
            id = id,
            handle = cEntity,
            data = data,
            active = startActive,
            behaviors = behaviors,
            collisionGroup = collisionGroup,
            collisionMask = collisionMask,
            physicsEnabled = enablePhysics,
            isKinematic = isKinematic
        )

        return EntityInfo(entity = entity, behaviors = behaviors).apply {
            val ref = StableRef.create(this)
            c_attachKotlinEntity!!.invoke(cEntity, ref.asCPointer())
            stableRef = ref

            if (enablePhysics) {
                c_initEntityPhysics!!.invoke(cEntity, (scene as SceneImpl).physicsHandle, isKinematic)
            }
        }
    }
}