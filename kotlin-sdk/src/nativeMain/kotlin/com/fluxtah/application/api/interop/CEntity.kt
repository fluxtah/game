package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateEntityInfo
import com.fluxtah.application.api.interop.model.EntityArray
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CEntity = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateEntityInfo = CPointer<CreateEntityInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateEntityFunc = (CApplicationContext, CCreateEntityInfo) -> CEntity

@OptIn(ExperimentalForeignApi::class)
var c_createEntity: CreateEntityFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateEntityFunc")
fun ktSetCreateEntityFunc(fn: CPointer<CFunction<CreateEntityFunc>>) {
    c_createEntity = { context, info ->
        memScoped {
            fn.reinterpret<CFunction<CreateEntityFunc>>()(
                context,
                info
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyEntityFunc = (CApplicationContext, CEntity) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyEntity: DestroyEntityFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyEntityFunc")
fun ktSetDestroyEntityFunc(fn: CPointer<CFunction<(CApplicationContext, CEntity) -> Unit>>) {
    c_destroyEntity = { device, entity ->
        memScoped {
            fn.reinterpret<CFunction<(CApplicationContext, CEntity) -> Unit>>()(
                device,
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntities")
fun ktGetEntities(): CPointer<EntityArray> {

    // if(activeSceneInfo.scene == Scene.EMPTY) return null

    val scene = activeSceneInfo.scene
    val entities =
        scene.entityPools.values.flatMap {
            it.entityPool.entitiesInUse.filter { it.entity.visible }.map { it.entity.handle }
        } +
                scene.entities.values.filter { it.entity.visible && it.entity.active }.map { it.entity.handle }

    val entityPointerArray = nativeHeap.allocArray<COpaquePointerVar>(entities.size)

    entities.forEachIndexed { index, light ->
        entityPointerArray[index] = light
    }

    val entityArray = nativeHeap.alloc<EntityArray>()
    entityArray.entities = entityPointerArray
    entityArray.size = entities.size

    return entityArray.ptr
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityPositionFunc = (CEntity, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityPosition: EntityPositionFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPositionEntityFunc")
fun ktSetPositionEntityFunc(fn: CPointer<CFunction<(CEntity, Float, Float, Float) -> Unit>>) {
    c_setEntityPosition = { entity, x, y, z ->
        memScoped {
            fn.reinterpret<CFunction<(CEntity, Float, Float, Float) -> Unit>>()(
                entity, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityRotationFunc = (CEntity, Float, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityRotation: EntityRotationFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityRotationFunc")
fun ktSetEntityRotationFunc(fn: CPointer<CFunction<EntityRotationFunc>>) {
    c_setEntityRotation = { entity, w, x, y, z ->
        memScoped {
            fn.reinterpret<CFunction<EntityRotationFunc>>()(
                entity, w, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityScaleFunc = (CEntity, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityScale: EntityScaleFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityScaleFunc")
fun ktSetEntityScaleFunc(fn: CPointer<CFunction<EntityScaleFunc>>) {
    c_setEntityScale = { entity, x, y, z ->
        memScoped {
            fn.reinterpret<CFunction<EntityScaleFunc>>()(
                entity, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityVelocityFunc = (CEntity, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityVelocity: EntityVelocityFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityVelocityFunc")
fun ktSetEntityVelocityFunc(fn: CPointer<CFunction<EntityVelocityFunc>>) {
    c_setEntityVelocity = { entity, x, y, z ->
        memScoped {
            fn.reinterpret<CFunction<EntityVelocityFunc>>()(
                entity, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityMassFunc = (CEntity, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityMass: EntityMassFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityMassFunc")
fun ktSetEntityMassFunc(fn: CPointer<CFunction<EntityMassFunc>>) {
    c_setEntityMass = { entity, mass ->
        memScoped {
            fn.reinterpret<CFunction<EntityMassFunc>>()(
                entity, mass
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetPositionXFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityPositionX: EntityGetPositionXFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityPositionXFunc")
fun ktGetEntityPositionXFunc(fn: CPointer<CFunction<EntityGetPositionXFunc>>) {
    c_getEntityPositionX = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetPositionXFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetPositionYFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityPositionY: EntityGetPositionYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityPositionYFunc")
fun ktGetEntityPositionYFunc(fn: CPointer<CFunction<EntityGetPositionYFunc>>) {
    c_getEntityPositionY = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetPositionYFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetPositionZFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityPositionZ: EntityGetPositionZFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityPositionZFunc")
fun ktGetEntityPositionZFunc(fn: CPointer<CFunction<EntityGetPositionZFunc>>) {
    c_getEntityPositionZ = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetPositionZFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetRotationWFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityRotationW: EntityGetRotationWFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityRotationWFunc")
fun ktGetEntityRotationWFunc(fn: CPointer<CFunction<EntityGetRotationWFunc>>) {
    c_getEntityRotationW = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetRotationWFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetRotationXFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityRotationX: EntityGetRotationXFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityRotationXFunc")
fun ktGetEntityRotationXFunc(fn: CPointer<CFunction<EntityGetRotationXFunc>>) {
    c_getEntityRotationX = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetRotationXFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetRotationYFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityRotationY: EntityGetRotationYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityRotationYFunc")
fun ktGetEntityRotationYFunc(fn: CPointer<CFunction<EntityGetRotationYFunc>>) {
    c_getEntityRotationY = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetRotationYFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetRotationZFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityRotationZ: EntityGetRotationYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityRotationZFunc")
fun ktGetEntityRotationZFunc(fn: CPointer<CFunction<EntityGetRotationZFunc>>) {
    c_getEntityRotationZ = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetRotationZFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetScaleXFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityScaleX: EntityGetScaleXFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityScaleXFunc")
fun ktGetEntityScaleXFunc(fn: CPointer<CFunction<EntityGetScaleXFunc>>) {
    c_getEntityScaleX = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetScaleXFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetScaleYFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityScaleY: EntityGetScaleYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityScaleYFunc")
fun ktGetEntityScaleYFunc(fn: CPointer<CFunction<EntityGetScaleYFunc>>) {
    c_getEntityScaleY = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetScaleYFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetScaleZFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityScaleZ: EntityGetScaleYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityScaleZFunc")
fun ktGetEntityScaleZFunc(fn: CPointer<CFunction<EntityGetScaleZFunc>>) {
    c_getEntityScaleZ = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetScaleZFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetVelocityXFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityVelocityX: EntityGetVelocityXFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityVelocityXFunc")
fun ktGetEntityVelocityXFunc(fn: CPointer<CFunction<EntityGetVelocityXFunc>>) {
    c_getEntityVelocityX = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetVelocityXFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetVelocityYFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityVelocityY: EntityGetVelocityYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityVelocityYFunc")
fun ktGetEntityVelocityYFunc(fn: CPointer<CFunction<EntityGetVelocityYFunc>>) {
    c_getEntityVelocityY = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetVelocityYFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetVelocityZFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityVelocityZ: EntityGetVelocityYFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityVelocityZFunc")
fun ktGetEntityVelocityZFunc(fn: CPointer<CFunction<EntityGetVelocityZFunc>>) {
    c_getEntityVelocityZ = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetVelocityZFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityGetMassFunc = (CEntity) -> Float

@OptIn(ExperimentalForeignApi::class)
var c_getEntityMass: EntityGetMassFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityMassFunc")
fun ktGetEntityMassFunc(fn: CPointer<CFunction<EntityGetMassFunc>>) {
    c_getEntityMass = { entity ->
        memScoped {
            fn.reinterpret<CFunction<EntityGetMassFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias AttachKotlinEntityFunc = (CEntity, COpaquePointer) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_attachKotlinEntity: AttachKotlinEntityFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetAttachKotlinEntityFunc")
fun ktSetAttachKotlinEntityFunc(fn: CPointer<CFunction<AttachKotlinEntityFunc>>) {
    c_attachKotlinEntity = { entity, kotlinEntityPtr ->
        memScoped {
            fn.reinterpret<CFunction<AttachKotlinEntityFunc>>()(
                entity, kotlinEntityPtr
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetEntitySkinIndexFunc = (CEntity, Int) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntitySkinIndex: SetEntitySkinIndexFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntitySkinIndexFunc")
fun ktSetEntitySkinIndexFunc(fn: CPointer<CFunction<SetEntitySkinIndexFunc>>) {
    c_setEntitySkinIndex = { entity, index ->
        memScoped {
            fn.reinterpret<CFunction<SetEntitySkinIndexFunc>>()(
                entity, index
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias InitEntityPhysicsFunc = (CEntity, CPhysics, Boolean) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_initEntityPhysics: InitEntityPhysicsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetInitEntityPhysicsFunc")
fun ktSetInitEntityPhysicsFunc(fn: CPointer<CFunction<InitEntityPhysicsFunc>>) {
    c_initEntityPhysics = { entity, physics, isKinematic ->
        memScoped {
            fn.reinterpret<CFunction<InitEntityPhysicsFunc>>()(
                entity, physics, isKinematic
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias RemoveEntityPhysicsFunc = (CEntity, CPhysics) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_removeEntityPhysics: RemoveEntityPhysicsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetRemoveEntityPhysicsFunc")
fun ktSetRemoveEntityPhysicsFunc(fn: CPointer<CFunction<RemoveEntityPhysicsFunc>>) {
    c_removeEntityPhysics = { entity, physics ->
        memScoped {
            fn.reinterpret<CFunction<RemoveEntityPhysicsFunc>>()(
                entity, physics
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias UpdateEntityPhysicsTransformFunc = (CEntity) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_updateEntityPhysicsTransform: UpdateEntityPhysicsTransformFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetUpdateEntityPhysicsTransformFunc")
fun ktSetUpdateEntityPhysicsTransformFunc(fn: CPointer<CFunction<UpdateEntityPhysicsTransformFunc>>) {
    c_updateEntityPhysicsTransform = { entity ->
        memScoped {
            fn.reinterpret<CFunction<UpdateEntityPhysicsTransformFunc>>()(
                entity
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityPhysicsActiveFunc = (CEntity, Boolean) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityPhysicsActive: EntityPhysicsActiveFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityPhysicsActiveFunc")
fun ktSetEntityPhysicsActiveFunc(fn: CPointer<CFunction<EntityPhysicsActiveFunc>>) {
    c_setEntityPhysicsActive = { entity, active ->
        memScoped {
            fn.reinterpret<CFunction<EntityPhysicsActiveFunc>>()(
                entity, active
            )
        }
    }
}
