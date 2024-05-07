package com.fluxtah.application.api.interop

import com.fluxtah.application.api.scene.activeSceneInfo
import com.fluxtah.application.api.interop.model.CreateLightInfo
import com.fluxtah.application.api.interop.model.LightArray
import com.fluxtah.application.api.scene.BaseScene
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CLight = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateLightInfo = CPointer<CreateLightInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateLightFunc = (CCreateLightInfo) -> CLight

@OptIn(ExperimentalForeignApi::class)
var c_createLight: CreateLightFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateLightFunc")
fun ktSetCreateLightFunc(fn: CPointer<CFunction<(CCreateLightInfo) -> CLight>>) {
    c_createLight = { info ->
        memScoped {
            fn.reinterpret<CFunction<(CCreateLightInfo) -> CLight>>()(info)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyLightFunc = (CLight) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyLight: DestroyLightFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyLightFunc")
fun ktSetDestroyLightFunc(fn: CPointer<CFunction<(CLight) -> Unit>>) {
    c_destroyLight = { light ->
        memScoped {
            fn.reinterpret<CFunction<(CLight) -> Unit>>()(light)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetLights")
fun ktGetLights(): CPointer<LightArray> {
    val lights = activeSceneInfo.scene.lights.values.map { it.handle } // Assuming handle is COpaquePointer
    val lightsArray = nativeHeap.allocArray<COpaquePointerVar>(lights.size)

    lights.forEachIndexed { index, light ->
        lightsArray[index] = light
    }

    val lightArray = nativeHeap.alloc<LightArray>()
    lightArray.lights = lightsArray
    lightArray.size = lights.size

    return lightArray.ptr
}

