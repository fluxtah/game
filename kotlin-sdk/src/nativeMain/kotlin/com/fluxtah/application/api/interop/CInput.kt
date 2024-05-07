package com.fluxtah.application.api.interop

import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias IsKeyPressedFunc = (Int) -> Int

@OptIn(ExperimentalForeignApi::class)
var c_isKeyPressed: IsKeyPressedFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetIsKeyPressedFunc")
fun ktSetIsKeyPressedFunc(fn: CPointer<CFunction<(Int) -> Int>>) {
    c_isKeyPressed = { key ->
        memScoped {
            fn.reinterpret<CFunction<(Int) -> Int>>()(key)
        }
    }
}
