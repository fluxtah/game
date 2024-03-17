package com.fluxtah.application.api.interop

import com.fluxtah.application.api.ApplicationContext
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CApplicationContext = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetApplicationContext")
fun ktSetApplicationContext(context: CApplicationContext) {
    ApplicationContext.vulcanContext = context
}

@OptIn(ExperimentalForeignApi::class)
typealias SetActiveCameraFunc = (CApplicationContext, CCamera) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setActiveCamera: SetActiveCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetActiveCameraFunc")
fun ktSetActiveCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CApplicationContext, CCamera) -> Unit>>) {
    c_setActiveCamera = { context, camera ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CApplicationContext, CCamera) -> Unit>>()(context, camera)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetEnableDebugBoundingVolumesFunc = (CApplicationContext, Boolean) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setEnableDebugBoundingVolumes: SetEnableDebugBoundingVolumesFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetEnableDebugBoundingVolumesFunc")
fun ktSetEnableDebugBoundingVolumesFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CApplicationContext, Boolean) -> Unit>>) {
    c_setEnableDebugBoundingVolumes = { context, enable ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CApplicationContext, Boolean) -> Unit>>()(context, enable)
        }
    }
}