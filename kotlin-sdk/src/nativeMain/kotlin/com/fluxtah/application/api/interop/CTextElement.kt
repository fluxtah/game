package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateTextElementInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CTextElement = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateTextElementInfo = CPointer<CreateTextElementInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateTextElementFunc = (CSpriteSheet, CCreateTextElementInfo) -> CTextElement

@OptIn(ExperimentalForeignApi::class)
var c_createTextElement: CreateTextElementFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateTextElementFunc")
fun ktSetCreateTextElementFunc(fn: CPointer<CFunction<CreateTextElementFunc>>) {
    c_createTextElement = { spriteSheet, info ->
        memScoped {
            fn.reinterpret<CFunction<CreateTextElementFunc>>()(
                spriteSheet, info
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MeasureTextElementWidthFunc = (CTextElement) -> Int

@OptIn(ExperimentalForeignApi::class)
var c_measureTextElementWidth: MeasureTextElementWidthFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMeasureTextElementWidthFunc")
fun ktSetMeasureTextElementWidthFunc(fn: CPointer<CFunction<MeasureTextElementWidthFunc>>) {
    c_measureTextElementWidth = { element ->
        memScoped {
            fn.reinterpret<CFunction<MeasureTextElementWidthFunc>>()(element)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias MeasureTextElementHeightFunc = (CTextElement) -> Int

@OptIn(ExperimentalForeignApi::class)
var c_measureTextElementHeight: MeasureTextElementHeightFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetMeasureTextElementHeightFunc")
fun ktSetMeasureTextElementHeightFunc(fn: CPointer<CFunction<MeasureTextElementHeightFunc>>) {
    c_measureTextElementHeight = { element ->
        memScoped {
            fn.reinterpret<CFunction<MeasureTextElementHeightFunc>>()(element)
        }
    }
}
