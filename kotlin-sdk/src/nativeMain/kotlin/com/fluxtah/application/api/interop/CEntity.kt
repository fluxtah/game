package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CCollisionInfo
import com.fluxtah.application.api.interop.model.CreateEntityInfo
import com.fluxtah.application.api.interop.model.EntityArray
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
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
fun ktSetCreateEntityFunc(callback: CPointer<CFunction<CreateEntityFunc>>) {
    c_createEntity = { context, info ->
        memScoped {
            callback.reinterpret<CFunction<CreateEntityFunc>>()(
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
fun ktSetDestroyEntityFunc(callback: CPointer<CFunction<(CApplicationContext, CEntity) -> Unit>>) {
    c_destroyEntity = { device, entity ->
        memScoped {
            callback.reinterpret<CFunction<(CApplicationContext, CEntity) -> Unit>>()(device, entity)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntities")
fun ktGetEntities(): CPointer<EntityArray> {

   // if(activeSceneInfo.scene == Scene.EMPTY) return null

    val scene = activeSceneInfo.scene
    val entities =
            scene.entityPools.values.flatMap { it.entityPool.entitiesInUse.filter { it.entity.visible }.map { it.entity.handle } } +
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
fun ktSetPositionEntityFunc(callback: CPointer<CFunction<(CEntity, Float, Float, Float) -> Unit>>) {
    c_setEntityPosition = { entity, x, y, z ->
        memScoped {
            callback.reinterpret<CFunction<(CEntity, Float, Float, Float) -> Unit>>()(
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
fun ktSetEntityRotationFunc(callback: CPointer<CFunction<EntityRotationFunc>>) {
    c_setEntityRotation = { entity, x, y, z ->
        memScoped {
            callback.reinterpret<CFunction<EntityRotationFunc>>()(
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
fun ktSetEntityScaleFunc(callback: CPointer<CFunction<EntityScaleFunc>>) {
    c_setEntityScale = { entity, x, y, z ->
        memScoped {
            callback.reinterpret<CFunction<EntityScaleFunc>>()(
                entity, x, y, z
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
fun ktSetAttachKotlinEntityFunc(callback: CPointer<CFunction<AttachKotlinEntityFunc>>) {
    c_attachKotlinEntity = { entity, kotlinEntityPtr ->
        memScoped {
            callback.reinterpret<CFunction<AttachKotlinEntityFunc>>()(
                entity, kotlinEntityPtr
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias GetEntityCollisionInfoFunc = (CEntity, CEntity, Int, Int) -> CValue<CCollisionInfo>

@OptIn(ExperimentalForeignApi::class)
var c_getEntityCollisionInfo: GetEntityCollisionInfoFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetEntityCollisionInfoFunc")
fun ktGetEntityCollisionInfoFunc(callback: CPointer<CFunction<GetEntityCollisionInfoFunc>>) {
    c_getEntityCollisionInfo = { a, b, aAabbIndex, bAabbIndex ->
        memScoped {
            callback.reinterpret<CFunction<GetEntityCollisionInfoFunc>>()(
                a, b, aAabbIndex, bAabbIndex
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
fun ktSetEntitySkinIndexFunc(callback: CPointer<CFunction<SetEntitySkinIndexFunc>>) {
    c_setEntitySkinIndex = { entity, index ->
        memScoped {
            callback.reinterpret<CFunction<SetEntitySkinIndexFunc>>()(
                entity, index
            )
        }
    }
}

//@OptIn(ExperimentalForeignApi::class)
//typealias GetEntityAabbFunc = (CEntity) -> CValue<AABB>
//
//@OptIn(ExperimentalForeignApi::class)
//var c_getEntityAABB: GetEntityAabbFunc? = null
//
//@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
//@CName("ktGetEntityAabbFunc")
//fun ktGetEntityAabbFunc(callback: CPointer<CFunction<GetEntityAabbFunc>>) {
//    c_getEntityAABB = { a ->
//        memScoped {
//            callback.reinterpret<CFunction<GetEntityAabbFunc>>()(
//                a
//            )
//        }
//    }
//}

