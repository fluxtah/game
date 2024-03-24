package com.fluxtah.application.api.entity

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.file.toAssetsPath
import com.fluxtah.application.api.interop.c_attachKotlinEntity
import com.fluxtah.application.api.interop.c_createEntity
import com.fluxtah.application.api.interop.c_initEntityPhysics
import com.fluxtah.application.api.interop.model.CreateEntityInfo
import com.fluxtah.application.api.scene.EntityInfo
import com.fluxtah.application.api.scene.OnSceneAfterEntityUpdate
import com.fluxtah.application.api.scene.OnSceneBeforeEntityUpdate
import com.fluxtah.application.api.scene.OnSceneEntityUpdate
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.SceneDsl
import com.fluxtah.application.api.scene.SceneImpl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped

@SceneDsl
@OptIn(ExperimentalForeignApi::class)
class EntityBuilder(private val scene: Scene, private val id: String, private val modelPath: String) {
    private var positionX: Float = 0.0f
    private var positionY: Float = 0.0f
    private var positionZ: Float = 0.0f
    private var rotationW: Float = 1.0f
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

    private var startActive: Boolean = true

    private var collisionGroup: Int = 0
    private var collisionMask: Int = 0

    private var skinIndex = 0

    private var onSceneEntityUpdate: OnSceneEntityUpdate? = null
    private var onSceneBeforeEntityUpdate: OnSceneBeforeEntityUpdate? = null
    private var onSceneAfterEntityUpdate: OnSceneAfterEntityUpdate? = null

    private val behaviors = mutableListOf<() -> EntityBehavior>()

    fun <T : Any> data(block: () -> T) {
        data = block
    }

    fun startActive(active: Boolean = true) {
        startActive = active
    }

    fun physics(enabled: Boolean = true) {
        enablePhysics = enabled
    }

    fun kinematic(enabled: Boolean = true) {
        isKinematic = enabled
    }

    fun position(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        positionX = x
        positionY = y
        positionZ = z
    }

    fun rotation(w: Float = 1f, x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        rotationW = w
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

    fun onSceneUpdate(block: OnSceneEntityUpdate) {
        onSceneEntityUpdate = block
    }

    fun onSceneBeforeUpdate(block: OnSceneBeforeEntityUpdate) {
        onSceneBeforeEntityUpdate = block
    }

    fun onSceneAfterUpdate(block: OnSceneAfterEntityUpdate) {
        onSceneAfterEntityUpdate = block
    }

    fun behaviour(behavior: () -> EntityBehavior) {
        behaviors.add(behavior)
    }

    fun collisionGroup(group: Int) {
        collisionGroup = group
    }

    fun collisionMask(mask: Int) {
        collisionMask = mask
    }

    fun skin(index: Int) {
        skinIndex = index
    }

    @OptIn(ExperimentalForeignApi::class)
    fun build(): EntityInfo = createEntityInfo()

    private fun createEntityInfo(): EntityInfo {

        val cEntity = memScoped {
            val info = cValue<CreateEntityInfo> {
                modelFileName = modelPath.toAssetsPath().cstr.ptr
                positionX = this@EntityBuilder.positionX
                positionY = this@EntityBuilder.positionY
                positionZ = this@EntityBuilder.positionZ
                rotationW = this@EntityBuilder.rotationW
                rotationX = this@EntityBuilder.rotationX
                rotationY = this@EntityBuilder.rotationY
                rotationZ = this@EntityBuilder.rotationZ
                scaleX = this@EntityBuilder.scaleX
                scaleY = this@EntityBuilder.scaleY
                scaleZ = this@EntityBuilder.scaleZ
                velocityX = this@EntityBuilder.velocityX
                velocityY = this@EntityBuilder.velocityY
                velocityZ = this@EntityBuilder.velocityZ
                mass = this@EntityBuilder.mass
                collisionGroup = this@EntityBuilder.collisionGroup
                collisionMask = this@EntityBuilder.collisionMask
                skinIndex = this@EntityBuilder.skinIndex
            }
            c_createEntity!!.invoke(ApplicationContext.vulcanContext!!, info.ptr)
        }

        val behaviors = behaviors.map { it().apply { this.scene = this@EntityBuilder.scene } }

        val data = data()

        val entity = Entity(
            id = id,
            handle = cEntity,
            data = data,
            startActive = startActive,
            visible = true,
            behaviors = behaviors,
            collisionGroup = collisionGroup,
            collisionMask = collisionMask,
            physicsEnabled = enablePhysics,
            isKinematic = isKinematic
        )

        return EntityInfo(
            entity = entity,
            onSceneEntityUpdate = onSceneEntityUpdate,
            onSceneBeforeEntityUpdate = onSceneBeforeEntityUpdate,
            onSceneAfterEntityUpdate = onSceneAfterEntityUpdate,
            behaviors = behaviors,
        ).apply {
            val ref = StableRef.create(this)
            c_attachKotlinEntity!!.invoke(cEntity, ref.asCPointer())
            stableRef = ref

            if(enablePhysics) {
                c_initEntityPhysics!!.invoke(cEntity, (scene as SceneImpl).physicsHandle, isKinematic)
            }
        }
    }
}