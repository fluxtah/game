package com.fluxtah.application.api.interop

import com.fluxtah.application.api.interop.model.TextBatchArray
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.activeSceneInfo
import kotlinx.cinterop.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
typealias CTextBatch = CPointer<CPointed>

@OptIn(ExperimentalForeignApi::class)
typealias CreateTextBatchFunc = (CSpriteSheet) -> CTextBatch

@OptIn(ExperimentalForeignApi::class)
var c_createTextBatch: CreateTextBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetCreateTextBatchFunc")
fun ktSetCreateTextBatchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<CreateTextBatchFunc>>) {
    c_createTextBatch = { spriteSheet ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<CreateTextBatchFunc>>()(
                spriteSheet
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias DestroyTextBatchFunc = (CApplicationContext, CTextBatch) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_destroyTextBatch: DestroyTextBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetDestroyTextBatchFunc")
fun ktSetDestroyTextBatchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<DestroyTextBatchFunc>>) {
    c_destroyTextBatch = { context, textBatch ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<DestroyTextBatchFunc>>()(context, textBatch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias InitializeTextBatchFunc = (CApplicationContext, CTextBatch) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_initializeTextBatch: InitializeTextBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetInitializeTextBatchFunc")
fun ktSetInitializeTextBatchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<InitializeTextBatchFunc>>) {
    c_initializeTextBatch = { context, textBatch ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<InitializeTextBatchFunc>>()(context, textBatch)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias AddTextElementToBatchFunc = (CTextBatch, CTextElement) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_addTextElementToBatch: AddTextElementToBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetAddTextElementToBatchFunc")
fun ktSetAddTextElementToBatchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<AddTextElementToBatchFunc>>) {
    c_addTextElementToBatch = { textBatch, element ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<AddTextElementToBatchFunc>>()(textBatch, element)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias UpdateTextElementSegmentInBatchFunc = (CApplicationContext, CTextBatch, Int, Int, CPointer<ByteVar>) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_updateTextElementSegmentInBatch: UpdateTextElementSegmentInBatchFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetUpdateTextElementSegmentInBatchFunc")
fun ktSetUpdateTextElementSegmentInBatchFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<UpdateTextElementSegmentInBatchFunc>>) {
    c_updateTextElementSegmentInBatch = { context, batch, elementIndex, segmentIndex, newText ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<UpdateTextElementSegmentInBatchFunc>>()(
                context, batch, elementIndex, segmentIndex, newText
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
typealias UpdateTextElementPositionFunc = (CApplicationContext, CTextBatch, Int, Float, Float) -> Unit

@OptIn(ExperimentalForeignApi::class)
var c_updateTextElementPosition: UpdateTextElementPositionFunc? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktSetUpdateTextElementPositionFunc")
fun ktSetUpdateTextElementPositionFunc(rigidBodyTransformUpdatedCallback: CPointer<CFunction<UpdateTextElementPositionFunc>>) {
    c_updateTextElementPosition = { context, batch, elementIndex, x, y ->
        memScoped {
            rigidBodyTransformUpdatedCallback.reinterpret<CFunction<UpdateTextElementPositionFunc>>()(
                context, batch, elementIndex, x, y
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("ktGetTextBatches")
fun ktGetTextBatches(): CPointer<TextBatchArray> {
    val scene = activeSceneInfo.scene
    val batches = scene.textBatches.values.filter { it.visible }.map { it.handle }
    val batchPointerArray = nativeHeap.allocArray<COpaquePointerVar>(batches.size)

    batches.forEachIndexed { index, light ->
        batchPointerArray[index] = light
    }

    val entityArray = nativeHeap.alloc<TextBatchArray>()
    entityArray.batches = batchPointerArray
    entityArray.size = batches.size

    return entityArray.ptr
}