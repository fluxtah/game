package com.fluxtah.application.api.interop

import com.fluxtah.application.api.entity.CollisionResult2
import com.fluxtah.application.api.interop.model.CCollisionResult2
import com.fluxtah.application.api.interop.model.CreateEmitterInfo
import com.fluxtah.application.api.interop.model.CreatePhysicsInfo
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.reinterpret
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CPhysics = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreatePhysicsInfo = CPointer<CreatePhysicsInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreatePhysicsFunc = (CCreatePhysicsInfo) -> CPhysics

@OptIn(ExperimentalForeignApi::class)
var c_createPhysics: CreatePhysicsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreatePhysicsFunc")
fun ktSetCreatePhysicsFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreatePhysicsFunc>>) {
    c_createPhysics = { info ->
        rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreatePhysicsFunc>>()(info)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyPhysicsFunc = (CPhysics) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyPhysics: DestroyPhysicsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyPhysicsFunc")
fun ktSetDestroyPhysicsFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<DestroyPhysicsFunc>>) {
    c_destroyPhysics = { physics ->
        rigidBodyTransformUpdatedCallback.reinterpret<CFunction<DestroyPhysicsFunc>>()(physics)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias StepPhysicsSimulationFunc = (CPhysics, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_stepPhysicsSimulation: StepPhysicsSimulationFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetStepPhysicsSimulationFunc")
fun ktSetStepPhysicsSimulationFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<StepPhysicsSimulationFunc>>) {
    c_stepPhysicsSimulation = { physics, deltaTime ->
        rigidBodyTransformUpdatedCallback.reinterpret<CFunction<StepPhysicsSimulationFunc>>()(physics, deltaTime)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias RigidBodyCallback = CPointer<CFunction<(
    entityInfo: COpaquePointer,
    x: Float, y: Float, z: Float,
    rotX: Float, rotY: Float, rotZ: Float
) -> Unit>>

@OptIn(ExperimentalForeignApi::class)
typealias SetOnRigidBodyUpdatedFunc = (CPhysics, RigidBodyCallback) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setOnRigidBodyUpdated: SetOnRigidBodyUpdatedFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetOnRigidBodyUpdatedFunc")
fun ktSetOnRigidBodyUpdatedFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<SetOnRigidBodyUpdatedFunc>>) {
    c_setOnRigidBodyUpdated = { physics, func ->
        rigidBodyTransformUpdatedCallback.reinterpret<CFunction<SetOnRigidBodyUpdatedFunc>>()(physics, func)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias CollisionCallback = CPointer<CFunction<(result: CCollisionResult2) -> Unit>>

@OptIn(ExperimentalForeignApi::class)
typealias SetCollisionCallbackFunc = (CPhysics, CollisionCallback) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setCollisionCallback: SetCollisionCallbackFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCollisionCallbackFunc")
fun ktSetCollisionCallbackFunc(collisionCallback: CPointer<CFunction<SetCollisionCallbackFunc>>) {
    c_setCollisionCallback = { context, func ->
        collisionCallback.reinterpret<CFunction<SetCollisionCallbackFunc>>()(context, func)
    }
}

//@OptIn(ExperimentalForeignApi::class)
//external fun setOnRigidBodyUpdatedFunction(
//    context: CPhysics,
//    rigidBodyTransformUpdatedCallback: CPointer<CFunction<(
//        entity: CEntity,
//        x: Float, y: Float, z: Float,
//        rotX: Float, rotY: Float, rotZ: Float
//    ) -> Unit>>
//)
