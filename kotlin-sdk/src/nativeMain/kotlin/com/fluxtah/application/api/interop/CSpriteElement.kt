package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateSpriteElementInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CSpriteElement = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateSpriteElementInfo = CPointer<CreateSpriteElementInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateSpriteElementFunc = (CSpriteSheet, CCreateSpriteElementInfo) -> CSpriteElement

@OptIn(ExperimentalForeignApi::class)
var c_createSpriteElement: CreateSpriteElementFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateSpriteElementFunc")
fun ktSetCreateSpriteElementFunc(callback: CPointer<CFunction<CreateSpriteElementFunc>>) {
    c_createSpriteElement = { spriteSheet, info ->
        memScoped {
            callback.reinterpret<CFunction<CreateSpriteElementFunc>>()(
                spriteSheet, info
            )
        }
    }
}