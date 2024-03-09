package com.fluxtah.application.api.sprite

import com.fluxtah.application.api.interop.CSpriteBatch
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class SpriteBatch(
    val id: String,
    val handle: CSpriteBatch,
    private val spriteElements: List<SpriteElement>,
    var visible: Boolean = true,
) {
    /**
     * Updates the sprite sheet index for the given element by index in the batch.
     */
    fun elementAt(elementIndex: Int): SpriteElement = spriteElements[elementIndex]
}