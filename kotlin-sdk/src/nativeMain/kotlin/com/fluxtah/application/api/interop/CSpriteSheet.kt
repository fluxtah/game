package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.CreateSpriteSheetInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CSpriteSheet = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CCreateSpriteSheetInfo = CPointer<CreateSpriteSheetInfo>

@OptIn(ExperimentalForeignApi::class)
typealias CreateSpriteSheetFunc = (CApplicationContext, CCreateSpriteSheetInfo) -> CSpriteSheet

@OptIn(ExperimentalForeignApi::class)
var c_createSpriteSheet: CreateSpriteSheetFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateSpriteSheetFunc")
fun ktSetCreateSpriteSheetFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreateSpriteSheetFunc>>) {
    c_createSpriteSheet = { context, info ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreateSpriteSheetFunc>>()(
                context, info
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroySpriteSheetFunc = (CApplicationContext, CSpriteSheet) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroySpriteSheet: DestroySpriteSheetFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroySpriteSheetFunc")
fun ktSetDestroySpriteSheetFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<DestroySpriteSheetFunc>>) {
    c_destroySpriteSheet = { context, spriteSheet ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<DestroySpriteSheetFunc>>()(context, spriteSheet)
        }
    }
}