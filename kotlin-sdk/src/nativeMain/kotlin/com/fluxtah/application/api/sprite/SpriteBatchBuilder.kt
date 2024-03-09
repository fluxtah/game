package com.fluxtah.application.api.sprite

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.interop.CSpriteBatch
import com.fluxtah.application.api.interop.CSpriteSheet
import com.fluxtah.application.api.interop.c_addSpriteElementToBatch
import com.fluxtah.application.api.interop.c_createSpriteBatch
import com.fluxtah.application.api.interop.c_createSpriteElement
import com.fluxtah.application.api.interop.c_initializeSpriteBatch
import com.fluxtah.application.api.interop.model.CreateSpriteElementInfo
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped

@OptIn(ExperimentalForeignApi::class)
class SpriteBatchBuilder(
    private val scene: Scene,
    private val id: String,
    private val spriteSheetId: String
) {
    private val spriteElements = mutableListOf<(SpriteSheet, CSpriteBatch, Int) -> SpriteElement>()
    private var visible = true

    fun visible(visible: Boolean) {
        this.visible = visible
    }

    fun sprite(spriteSheetIndex: Int, builder: SpriteElementBuilder.() -> Unit) {
        spriteElements.add { spriteSheet, batch, index ->
            SpriteElementBuilder(index, batch, spriteSheetIndex).apply(builder).build(spriteSheet.handle)
        }
    }

    fun build(): SpriteBatch {
        val spriteSheet = (scene as BaseScene).spriteSheets[spriteSheetId]
            ?: throw Exception("Sprite sheet with id $spriteSheetId does not exist")

        val cSpriteBatch = c_createSpriteBatch!!.invoke(spriteSheet.handle)
        val elements = mutableListOf<SpriteElement>()

        spriteElements.forEachIndexed { index, it ->
            val spriteElement = it.invoke(spriteSheet, cSpriteBatch, index)
            elements.add(spriteElement)
            c_addSpriteElementToBatch!!.invoke(cSpriteBatch, spriteElement.handle)
        }

        c_initializeSpriteBatch!!.invoke(ApplicationContext.vulcanContext!!, cSpriteBatch)

        return SpriteBatch(id, cSpriteBatch, elements, visible)
    }

    class SpriteElementBuilder(
        private val spriteBatchIndex: Int,
        private val cSpriteBatch: CSpriteBatch,
        private val spriteSheetIndex: Int
    ) {

        private var positionX: Float = 0.0f
        private var positionY: Float = 0.0f
        private var colorR: Float = 1.0f
        private var colorG: Float = 1.0f
        private var colorB: Float = 1.0f
        private var colorA: Float = 1.0f

        private var scale: Float = 1.0f
        private var rotation: Float = 0.0f
        private var startCrop: Float = 0.0f
        private var endCrop: Float = 1.0f

        fun position(x: Float = 0f, y: Float = 0f) {
            positionX = x
            positionY = y
        }

        fun scale(scale: Float = 1f) {
            this.scale = scale
        }

        fun rotation(rotation: Float = 0f) {
            this.rotation = rotation
        }

        fun cropWidth(startCrop: Float = 0f, endCrop: Float = 1f) {
            this.startCrop = startCrop
            this.endCrop = endCrop
        }

        fun color(r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f) {
            colorR = r
            colorG = g
            colorB = b
            colorA = a
        }

        fun build(spriteSheet: CSpriteSheet): SpriteElement {

            val cSpriteElement = memScoped {
                val info = cValue<CreateSpriteElementInfo> {
                    spriteSheetIndex = this@SpriteElementBuilder.spriteSheetIndex.toLong()
                    positionX = this@SpriteElementBuilder.positionX
                    positionY = this@SpriteElementBuilder.positionY
                    colorR = this@SpriteElementBuilder.colorR
                    colorG = this@SpriteElementBuilder.colorG
                    colorB = this@SpriteElementBuilder.colorB
                    colorA = this@SpriteElementBuilder.colorA
                    scale = this@SpriteElementBuilder.scale
                    rotation = this@SpriteElementBuilder.rotation
                    startCrop = this@SpriteElementBuilder.startCrop
                    endCrop = this@SpriteElementBuilder.endCrop
                }
                c_createSpriteElement!!.invoke(spriteSheet, info.ptr)
            }

            return SpriteElement(
                spriteBatchIndex = spriteBatchIndex,
                spriteBatchHandle = cSpriteBatch,
                handle = cSpriteElement,
                initialSpriteSheetIndex = spriteSheetIndex,
                initialPositionX = positionX,
                initialPositionY = positionY,
                initialScale = scale,
                initialRotation = rotation
            )

        }
    }
}

