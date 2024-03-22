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
fun ktSetCreateEntityFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreateEntityFunc>>) {
    c_createEntity = { context, info ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreateEntityFunc>>()(
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
fun ktSetDestroyEntityFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CApplicationContext, CEntity) -> Unit>>) {
    c_destroyEntity = { device, entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CApplicationContext, CEntity) -> Unit>>()(
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
fun ktSetPositionEntityFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CEntity, Float, Float, Float) -> Unit>>) {
    c_setEntityPosition = { entity, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CEntity, Float, Float, Float) -> Unit>>()(
                entity, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias EntityRotationFunc = (CEntity, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEntityRotation: EntityRotationFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEntityRotationFunc")
fun ktSetEntityRotationFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityRotationFunc>>) {
    c_setEntityRotation = { entity, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityRotationFunc>>()(
                entity, x, y, z
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
fun ktSetEntityScaleFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityScaleFunc>>) {
    c_setEntityScale = { entity, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityScaleFunc>>()(
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
fun ktSetEntityVelocityFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityVelocityFunc>>) {
    c_setEntityVelocity = { entity, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityVelocityFunc>>()(
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
fun ktSetEntityMassFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityMassFunc>>) {
    c_setEntityMass = { entity, mass ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityMassFunc>>()(
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
fun ktGetEntityPositionXFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetPositionXFunc>>) {
    c_getEntityPositionX = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetPositionXFunc>>()(
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
fun ktGetEntityPositionYFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetPositionYFunc>>) {
    c_getEntityPositionY = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetPositionYFunc>>()(
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
fun ktGetEntityPositionZFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetPositionZFunc>>) {
    c_getEntityPositionZ = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetPositionZFunc>>()(
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
fun ktGetEntityRotationXFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetRotationXFunc>>) {
    c_getEntityRotationX = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetRotationXFunc>>()(
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
fun ktGetEntityRotationYFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetRotationYFunc>>) {
    c_getEntityRotationY = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetRotationYFunc>>()(
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
fun ktGetEntityRotationZFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetRotationZFunc>>) {
    c_getEntityRotationZ = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetRotationZFunc>>()(
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
fun ktGetEntityScaleXFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetScaleXFunc>>) {
    c_getEntityScaleX = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetScaleXFunc>>()(
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
fun ktGetEntityScaleYFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetScaleYFunc>>) {
    c_getEntityScaleY = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetScaleYFunc>>()(
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
fun ktGetEntityScaleZFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetScaleZFunc>>) {
    c_getEntityScaleZ = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetScaleZFunc>>()(
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
fun ktGetEntityVelocityXFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetVelocityXFunc>>) {
    c_getEntityVelocityX = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetVelocityXFunc>>()(
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
fun ktGetEntityVelocityYFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetVelocityYFunc>>) {
    c_getEntityVelocityY = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetVelocityYFunc>>()(
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
fun ktGetEntityVelocityZFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetVelocityZFunc>>) {
    c_getEntityVelocityZ = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetVelocityZFunc>>()(
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
fun ktGetEntityMassFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<EntityGetMassFunc>>) {
    c_getEntityMass = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<EntityGetMassFunc>>()(
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
fun ktSetAttachKotlinEntityFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<AttachKotlinEntityFunc>>) {
    c_attachKotlinEntity = { entity, kotlinEntityPtr ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<AttachKotlinEntityFunc>>()(
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
fun ktSetEntitySkinIndexFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<SetEntitySkinIndexFunc>>) {
    c_setEntitySkinIndex = { entity, index ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<SetEntitySkinIndexFunc>>()(
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
fun ktSetInitEntityPhysicsFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<InitEntityPhysicsFunc>>) {
    c_initEntityPhysics = { entity, physics, isKinematic ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<InitEntityPhysicsFunc>>()(
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
fun ktSetRemoveEntityPhysicsFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<RemoveEntityPhysicsFunc>>) {
    c_removeEntityPhysics = { entity, physics ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<RemoveEntityPhysicsFunc>>()(
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
fun ktSetUpdateEntityPhysicsTransformFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<UpdateEntityPhysicsTransformFunc>>) {
    c_updateEntityPhysicsTransform = { entity ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<UpdateEntityPhysicsTransformFunc>>()(
                entity
            )
        }
    }
}
