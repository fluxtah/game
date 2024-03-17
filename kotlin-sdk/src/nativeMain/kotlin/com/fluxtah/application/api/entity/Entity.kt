package com.fluxtah.application.api.entity

import com.fluxtah.application.api.interop.CEntity
import com.fluxtah.application.api.interop.c_getEntityMass
import com.fluxtah.application.api.interop.c_getEntityPositionX
import com.fluxtah.application.api.interop.c_getEntityPositionY
import com.fluxtah.application.api.interop.c_getEntityPositionZ
import com.fluxtah.application.api.interop.c_getEntityRotationX
import com.fluxtah.application.api.interop.c_getEntityRotationY
import com.fluxtah.application.api.interop.c_getEntityRotationZ
import com.fluxtah.application.api.interop.c_getEntityScaleX
import com.fluxtah.application.api.interop.c_getEntityScaleY
import com.fluxtah.application.api.interop.c_getEntityScaleZ
import com.fluxtah.application.api.interop.c_getEntityVelocityX
import com.fluxtah.application.api.interop.c_getEntityVelocityY
import com.fluxtah.application.api.interop.c_getEntityVelocityZ
import com.fluxtah.application.api.interop.c_setEntityMass
import com.fluxtah.application.api.interop.c_setEntityPosition
import com.fluxtah.application.api.interop.c_setEntityRotation
import com.fluxtah.application.api.interop.c_setEntityScale
import com.fluxtah.application.api.interop.c_setEntitySkinIndex
import com.fluxtah.application.api.interop.c_setEntityVelocity
import com.fluxtah.application.api.interop.c_updateEntityPhysicsTransform
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class Entity(
    val id: String,
    val handle: CEntity,
    val data: Any = Any(),
    var active: Boolean = true,
    var visible: Boolean = true,
    var collisionGroup: Int = 0,
    var collisionMask: Int = 0,
    val behaviors: List<EntityBehavior>,
    val physicsEnabled: Boolean = false,
    val isKinematic: Boolean = false
) {
    init {
        behaviors.forEach { it.entity = this }
    }

    val positionX: Float
        get() {
            return c_getEntityPositionX!!.invoke(handle)
        }

    val positionY: Float
        get() {
            return c_getEntityPositionY!!.invoke(handle)
        }

    val positionZ: Float
        get() {
            return c_getEntityPositionZ!!.invoke(handle)
        }

    val rotationX: Float
        get() {
            return c_getEntityRotationX!!.invoke(handle)
        }

    val rotationY: Float
        get() {
            return c_getEntityRotationY!!.invoke(handle)
        }

    val rotationZ: Float
        get() {
            return c_getEntityRotationZ!!.invoke(handle)
        }

    val scaleX: Float
        get() {
            return c_getEntityScaleX!!.invoke(handle)
        }

    val scaleY: Float
        get() {
            return c_getEntityScaleY!!.invoke(handle)
        }

    val scaleZ: Float
        get() {
            return c_getEntityScaleZ!!.invoke(handle)
        }

    val velocityX: Float
        get() {
            return c_getEntityVelocityX!!.invoke(handle)
        }

    val velocityY: Float
        get() {
            return c_getEntityVelocityY!!.invoke(handle)
        }

    val velocityZ: Float
        get() {
            return c_getEntityVelocityZ!!.invoke(handle)
        }

    val mass: Float
        get() {
            return c_getEntityMass!!.invoke(handle)
        }

    fun setPosition(x: Float? = null, y: Float? = null, z: Float? = null) {
        val newPosX = x ?: positionX
        val newPosY = y ?: positionY
        val newPosZ = z ?: positionZ
        c_setEntityPosition!!.invoke(handle, newPosX, newPosY, newPosZ)
    }

    fun setRotation(x: Float? = null, y: Float? = null, z: Float? = null) {
        val newRotX = x ?: rotationX
        val newRotY = y ?: rotationY
        val newRotZ = z ?: rotationZ
        c_setEntityRotation!!.invoke(handle, newRotX, newRotY, newRotZ)
    }

    fun setScale(x: Float? = null, y: Float? = null, z: Float? = null) {
        val newScaleX = x ?: scaleX
        val newScaleY = y ?: scaleY
        val newScaleZ = z ?: scaleZ
        c_setEntityScale!!.invoke(handle, newScaleX, newScaleY, newScaleZ)
    }

    fun setVelocity(x: Float? = null, y: Float? = null, z: Float? = null) {
        val newVelocityX = x ?: velocityX
        val newVelocityY = y ?: velocityY
        val newVelocityZ = z ?: velocityZ

        c_setEntityVelocity!!.invoke(handle, newVelocityX, newVelocityY, newVelocityZ)
    }

    fun setMass(mass: Float) {
        c_setEntityMass!!.invoke(handle, mass)
    }

    fun rotate(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        val newRotationX = rotationX + x
        val newRotationY = rotationY + y
        val newRotationZ = rotationZ + z
        c_setEntityRotation!!.invoke(handle, newRotationX, newRotationY, newRotationZ)
    }

    fun translate(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f) {
        val newPositionX = positionX + x
        val newPositionY = positionY + y
        val newPositionZ = positionZ + z
        c_setEntityPosition!!.invoke(handle, newPositionX, newPositionY, newPositionZ)
    }

    fun updatePhysicsState() {
        if (physicsEnabled) {
            c_updateEntityPhysicsTransform!!.invoke(handle)
        }
    }

    fun setSkin(index: Int) {
        c_setEntitySkinIndex!!.invoke(handle, index)
    }

//    fun getAabb(): AxisAlignedBoundingBox {
//        return c_getEntityAABB!!.invoke(handle).useContents {
//            AxisAlignedBoundingBox(
//                Vector3(min[0], min[1], min[2]),
//                Vector3(max[0], max[1], max[2])
//            )
//        }
//    }

    @Suppress("UNCHECKED_CAST")
    fun <T> data(): T = data as T

    fun dataAsAny(): Any = data

    fun resetBehaviors() {
        behaviors.forEach { it.reset() }
    }

    inline fun <reified T : EntityBehavior> getBehaviorByType(): T {
        for (behavior in behaviors) {
            if (behavior is T) {
                return behavior
            }
        }
        throw RuntimeException("Entity does not have a behavior of type ${T::class.simpleName}")
    }
}

