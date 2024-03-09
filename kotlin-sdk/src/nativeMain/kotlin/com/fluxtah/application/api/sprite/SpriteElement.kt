package com.fluxtah.application.api.sprite

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.interop.CSpriteBatch
import com.fluxtah.application.api.interop.CSpriteElement
import com.fluxtah.application.api.interop.c_transformSpriteElementInBatch
import com.fluxtah.application.api.interop.c_updateSpriteElementIndexInBatch
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped

@OptIn(ExperimentalForeignApi::class)
class SpriteElement(
    val spriteBatchIndex: Int,
    val spriteBatchHandle: CSpriteBatch,
    val handle: CSpriteElement,
    initialSpriteSheetIndex: Int,
    initialPositionX: Float = 0.0f,
    initialPositionY: Float = 0.0f,
    initialScale: Float = 1.0f,
    initialRotation: Float = 0.0f,
    initialStartCrop: Float = 0.0f,
    initialEndCrop: Float = 1.0f,
) {

    private var _positionX: Float = initialPositionX
    val positionX: Float
        get() {
            return _positionX
        }

    private var _positionY: Float = initialPositionY
    val positionY: Float
        get() {
            return _positionY
        }

    private var _scale: Float = initialScale
    val scale: Float
        get() {
            return _scale
        }

    private var _rotation: Float = initialRotation
    val rotation: Float
        get() {
            return _rotation
        }

    private var _startCrop: Float = initialStartCrop
    val startCrop: Float
        get() {
            return _startCrop
        }

    private var _endCrop: Float = initialEndCrop
    val endCrop: Float
        get() {
            return _endCrop
        }

    private var _spriteSheetIndex: Int = initialSpriteSheetIndex
    val spriteSheetIndex: Int
        get() {
            return _spriteSheetIndex
        }


    /**
     * Updates the sprite sheet index for the given element by index in the batch.
     */
    fun setSpriteSheetIndex(spriteSheetIndex: Int) {
        _spriteSheetIndex = spriteSheetIndex
        memScoped {
            c_updateSpriteElementIndexInBatch!!.invoke(
                ApplicationContext.vulcanContext!!,
                spriteBatchHandle,
                spriteBatchIndex,
                spriteSheetIndex,
            )
        }
    }

    fun transform(x: Float = 0f, y: Float = 0f, rotation: Float = 0f, scale: Float = 1f, startCrop: Float = 0f, endCrop: Float = 1f) {
        _positionX = x
        _positionY = y
        _rotation = rotation
        _scale = scale
        _startCrop = startCrop
        _endCrop = endCrop
        cTransformSpriteElementInBatch()
    }

    fun position(x: Float = 0f, y: Float = 0f) {
        _positionX = x
        _positionY = y
        cTransformSpriteElementInBatch()
    }

    fun scale(scale: Float = 1f) {
        _scale = scale
        cTransformSpriteElementInBatch()
    }

    fun rotation(rotation: Float = 0f) {
        _rotation = rotation
        cTransformSpriteElementInBatch()
    }

    fun cropWidth(startCrop: Float = 0f, endCrop: Float = 1f) {
        _startCrop = startCrop
        _endCrop = endCrop
        cTransformSpriteElementInBatch()
    }

    private fun cTransformSpriteElementInBatch() {
        c_transformSpriteElementInBatch!!.invoke(
            ApplicationContext.vulcanContext!!,
            spriteBatchHandle,
            spriteBatchIndex,
            _positionX,
            _positionY,
            _scale,
            _rotation,
            _startCrop,
            _endCrop,
        )
    }
}