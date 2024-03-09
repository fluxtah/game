package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.SpriteBatchArray
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CSpriteBatch = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CreateSpriteBatchFunc = (CSpriteSheet) -> CSpriteBatch

@OptIn(ExperimentalForeignApi::class)
var c_createSpriteBatch: CreateSpriteBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateSpriteBatchFunc")
fun ktSetCreateSpriteBatchFunc(callback: CPointer<CFunction<CreateSpriteBatchFunc>>) {
    c_createSpriteBatch = { spriteSheet ->
        memScoped {
            callback.reinterpret<CFunction<CreateSpriteBatchFunc>>()(
                spriteSheet
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroySpriteBatchFunc = (CApplicationContext, CSpriteBatch) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroySpriteBatch: DestroySpriteBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroySpriteBatchFunc")
fun ktSetDestroySpriteBatchFunc(callback: CPointer<CFunction<DestroySpriteBatchFunc>>) {
    c_destroySpriteBatch = { context, spriteBatch ->
        memScoped {
            callback.reinterpret<CFunction<DestroySpriteBatchFunc>>()(context, spriteBatch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias InitializeSpriteBatchFunc = (CApplicationContext, CSpriteBatch) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_initializeSpriteBatch: InitializeSpriteBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetInitializeSpriteBatchFunc")
fun ktSetInitializeSpriteBatchFunc(callback: CPointer<CFunction<InitializeSpriteBatchFunc>>) {
    c_initializeSpriteBatch = { context, spriteBatch ->
        memScoped {
            callback.reinterpret<CFunction<InitializeSpriteBatchFunc>>()(context, spriteBatch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias AddSpriteElementToBatchFunc = (CSpriteBatch, CSpriteElement) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_addSpriteElementToBatch: AddSpriteElementToBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetAddSpriteElementToBatchFunc")
fun ktSetAddSpriteElementToBatchFunc(callback: CPointer<CFunction<AddSpriteElementToBatchFunc>>) {
    c_addSpriteElementToBatch = { spriteBatch, element ->
        memScoped {
            callback.reinterpret<CFunction<AddSpriteElementToBatchFunc>>()(spriteBatch, element)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias UpdateSpriteElementIndexInBatchFunc = (CApplicationContext, CSpriteBatch, Int, Int) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_updateSpriteElementIndexInBatch: UpdateSpriteElementIndexInBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetUpdateSpriteElementIndexInBatchFunc")
fun ktSetUpdateSpriteElementIndexInBatchFunc(callback: CPointer<CFunction<UpdateSpriteElementIndexInBatchFunc>>) {
    c_updateSpriteElementIndexInBatch = { context, batch, elementIndex, spriteSheetIndex ->
        memScoped {
            callback.reinterpret<CFunction<UpdateSpriteElementIndexInBatchFunc>>()(
                context, batch, elementIndex, spriteSheetIndex
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetSpriteBatches")
fun ktGetSpriteBatches(): CPointer<SpriteBatchArray> {
    val scene = activeSceneInfo.scene
    val batches = scene.spriteBatches.values.filter { it.visible }.map { it.handle }
    val batchPointerArray = nativeHeap.allocArray<COpaquePointerVar>(batches.size)

    batches.forEachIndexed { index, light ->
        batchPointerArray[index] = light
    }

    val entityArray = nativeHeap.alloc<SpriteBatchArray>()
    entityArray.batches = batchPointerArray
    entityArray.size = batches.size

    return entityArray.ptr
}

@OptIn(ExperimentalForeignApi::class)
typealias TransformSpriteElementInBatchFunc = (CApplicationContext, CSpriteBatch, Int, Float, Float, Float, Float, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_transformSpriteElementInBatch: TransformSpriteElementInBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetTransformSpriteElementInBatchFunc")
fun ktSetTransformSpriteElementInBatchFunc(callback: CPointer<CFunction<TransformSpriteElementInBatchFunc>>) {
    c_transformSpriteElementInBatch = { context, batch, elementIndex, x, y, scale, rot, startCrop, endCrop ->
        memScoped {
            callback.reinterpret<CFunction<TransformSpriteElementInBatchFunc>>()(
                context, batch, elementIndex, x, y, scale, rot, startCrop, endCrop
            )
        }
    }
}