package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateEmitterInfo
import com.fluxtah.application.api.interop.model.CreatePhysicsInfo
import kotlinx.cinterop.CFunction
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
fun ktSetCreatePhysicsFunc(callback: CPointer<CFunction<CreatePhysicsFunc>>) {
    c_createPhysics = { info ->
        callback.reinterpret<CFunction<CreatePhysicsFunc>>()(info)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyPhysicsFunc = (CPhysics) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyPhysics: DestroyPhysicsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyPhysicsFunc")
fun ktSetDestroyPhysicsFunc(callback: CPointer<CFunction<DestroyPhysicsFunc>>) {
    c_destroyPhysics = { physics ->
        callback.reinterpret<CFunction<DestroyPhysicsFunc>>()(physics)
    }
}