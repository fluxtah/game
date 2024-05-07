package com.fluxtah.application.api.interop

import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

typealias HelloFunc = (String) -> Unit

var helloFuncCallback: HelloFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("helloCallback")
fun helloCallback(fn: CPointer<CFunction<(CPointer<ByteVar>) -> Unit>>) {
    helloFuncCallback = { message ->
        memScoped {
            fn.reinterpret<CFunction<(CPointer<ByteVar>) -> Unit>>()(message.cstr.ptr)
        }
    }
}