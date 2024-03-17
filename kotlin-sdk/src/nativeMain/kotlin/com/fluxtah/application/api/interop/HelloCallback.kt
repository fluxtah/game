package com.fluxtah.application.api.interop

import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

typealias HelloFunc = (String) -> Unit

var helloFuncCallback: HelloFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("helloCallback")
fun helloCallback(rigidBodyTransformUpdatedCallback: CPointer<CFunction<(CPointer<ByteVar>) -> Unit>>) {
    helloFuncCallback = { message ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<(CPointer<ByteVar>) -> Unit>>()(message.cstr.ptr)
        }
    }
}