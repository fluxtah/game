package com.fluxtah.application.api.interop

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.interop.model.CreateCameraInfo
import com.fluxtah.application.api.math.Vector2
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CCamera = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateCameraInfo = CPointer<CreateCameraInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateCameraFunc = (CCreateCameraInfo) -> CCamera

@OptIn(ExperimentalForeignApi::class)
var c_createCamera: CreateCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateCameraFunc")
fun ktSetCreateCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreateCameraFunc>>) {
    c_createCamera = { info ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreateCameraFunc>>()(info)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyCameraFunc = (CCamera) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyCamera: DestroyCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyCameraFunc")
fun ktSetDestroyCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<DestroyCameraFunc>>) {
    c_destroyCamera = { camera ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<DestroyCameraFunc>>()(camera)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MoveCameraForwardFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_moveCameraForward: MoveCameraForwardFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMoveCameraForwardFunc")
fun ktSetMoveCameraForwardFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<MoveCameraForwardFunc>>) {
    c_moveCameraForward = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<MoveCameraForwardFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MoveCameraBackwardFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_moveCameraBackward: MoveCameraBackwardFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMoveCameraBackwardFunc")
fun ktSetMoveCameraBackwardFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<MoveCameraBackwardFunc>>) {
    c_moveCameraBackward = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<MoveCameraBackwardFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MoveCameraLeftFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_moveCameraLeft: MoveCameraLeftFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMoveCameraLeftFunc")
fun ktSetMoveCameraLeftFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<MoveCameraLeftFunc>>) {
    c_moveCameraLeft = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<MoveCameraLeftFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MoveCameraRightFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_moveCameraRight: MoveCameraRightFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMoveCameraRightFunc")
fun ktSetMoveCameraRightFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<MoveCameraRightFunc>>) {
    c_moveCameraRight = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<MoveCameraRightFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias PitchCameraFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_pitchCamera: PitchCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPitchCameraFunc")
fun ktSetPitchCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<PitchCameraFunc>>) {
    c_pitchCamera = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<PitchCameraFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias YawCameraFunc = (CCamera, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_yawCamera: YawCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetYawCameraFunc")
fun ktSetYawCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<YawCameraFunc>>) {
    c_yawCamera = { camera, amount ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<YawCameraFunc>>()(
                camera, amount
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias SetCameraLookAtFunc = (CCamera, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_setCameraLookAt: SetCameraLookAtFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCameraLookAtFunc")
fun ktSetCameraLookAtFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<SetCameraLookAtFunc>>) {
    c_setCameraLookAt = { camera, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<SetCameraLookAtFunc>>()(
                camera, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias PositionCameraFunc = (CCamera, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_positionCamera: PositionCameraFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetPositionCameraFunc")
fun ktSetPositionCameraFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<PositionCameraFunc>>) {
    c_positionCamera = { camera, x, y, z ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<PositionCameraFunc>>()(
                camera, x, y, z
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias ApplyCameraChangesFunc = (CCamera) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_applyCameraChanges: ApplyCameraChangesFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetApplyCameraChangesFunc")
fun ktSetApplyCameraChangesFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<ApplyCameraChangesFunc>>) {
    c_applyCameraChanges = { camera ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<ApplyCameraChangesFunc>>()(camera)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun getWorldToScreenPoint(positionX: Float, positionY: Float, positionZ: Float): Vector2 {
    memScoped {
        val x = alloc<FloatVar>()
        val y = alloc<FloatVar>()
        c_worldToScreenPoint?.invoke(ApplicationContext.vulcanContext!!, positionX, positionY, positionZ, x.ptr, y.ptr)
        return Vector2(x.value, y.value)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias WorldToScreenPointFunc = (CApplicationContext, Float, Float, Float, CPointer<FloatVar>, CPointer<FloatVar>) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_worldToScreenPoint: WorldToScreenPointFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetWorldToScreenPointFunc")
fun ktSetWorldToScreenPointFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<WorldToScreenPointFunc>>) {
    c_worldToScreenPoint = { context, x, y, z, outX, outY ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<WorldToScreenPointFunc>>()(
                context, x, y, z, outX, outY
            )
        }
    }
}
