package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CJoyAxes
import com.fluxtah.application.api.interop.model.CJoyButtons
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias GetJoystickAxesFunc = (Int) -> CValue<CJoyAxes>

@OptIn(ExperimentalForeignApi::class)
var c_getJoystickAxes: GetJoystickAxesFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetGetJoystickAxesFunc")
fun ktSetGetJoystickAxesFunc(fn: CPointer<CFunction<GetJoystickAxesFunc>>) {
    c_getJoystickAxes = { key ->
        fn.reinterpret<CFunction<GetJoystickAxesFunc>>()(key)
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias GetJoystickButtonsFunc = (Int) -> CValue<CJoyButtons>

@OptIn(ExperimentalForeignApi::class)
var c_getJoystickButtons: GetJoystickButtonsFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetGetJoystickButtonsFunc")
fun ktSetGetJoystickButtonsFunc(fn: CPointer<CFunction<GetJoystickButtonsFunc>>) {
    c_getJoystickButtons = { key ->
        memScoped {
            fn.reinterpret<CFunction<GetJoystickButtonsFunc>>()(key)
        }
    }
}
