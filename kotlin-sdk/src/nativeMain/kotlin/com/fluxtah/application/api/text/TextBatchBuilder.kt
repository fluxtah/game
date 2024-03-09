package com.fluxtah.application.api.text

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.getScreenWidth
import com.fluxtah.application.api.interop.CSpriteBatch
import com.fluxtah.application.api.interop.CSpriteSheet
import com.fluxtah.application.api.interop.CTextBatch
import com.fluxtah.application.api.interop.CTextElement
import com.fluxtah.application.api.interop.c_addTextElementToBatch
import com.fluxtah.application.api.interop.c_createTextBatch
import com.fluxtah.application.api.interop.c_createTextElement
import com.fluxtah.application.api.interop.c_initializeTextBatch
import com.fluxtah.application.api.interop.c_measureTextElementHeight
import com.fluxtah.application.api.interop.c_measureTextElementWidth
import com.fluxtah.application.api.interop.c_updateTextElementPosition
import com.fluxtah.application.api.interop.c_updateTextElementSegmentInBatch
import com.fluxtah.application.api.interop.model.CreateTextElementInfo
import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.sprite.SpriteSheet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCStringArray

@OptIn(ExperimentalForeignApi::class)
class TextBatchBuilder(
    private val scene: Scene,
    private val id: String,
    private val spriteSheetId: String
) {
    private val textElements = mutableListOf<(SpriteSheet, CSpriteBatch, Int) -> TextElement>()
    private var visible = true

    fun visible(visible: Boolean) {
        this.visible = visible
    }

    fun text(template: String, vararg args: String, builder: TextElementBuilder.() -> Unit) {
        textElements.add { spriteSheet, batch, index ->
            TextElementBuilder(index, batch, template, args.toList()).apply(builder).build(spriteSheet.handle)
        }
    }

    fun build(): TextBatch {
        val spriteSheet = (scene as BaseScene).spriteSheets[spriteSheetId]
            ?: throw Exception("Sprite sheet with id $spriteSheetId does not exist")

        val cTextBatch = c_createTextBatch!!.invoke(spriteSheet.handle)
        val elements = mutableListOf<TextElement>()

        textElements.forEachIndexed { index, it ->
            val element = it.invoke(spriteSheet, cTextBatch, index)
            elements.add(element)
            c_addTextElementToBatch!!.invoke(cTextBatch, element.handle)
        }

        c_initializeTextBatch!!.invoke(ApplicationContext.vulcanContext!!, cTextBatch)

        return TextBatch(id, cTextBatch, visible, elements)
    }

    class TextElementBuilder(
        private val elementIndex: Int,
        private val spriteBatchHandle: CSpriteBatch,
        private val template: String,
        private val params: List<String>
    ) {

        private var positionX: Float = 0.0f
        private var positionY: Float = 0.0f
        private var colorR: Float = 1.0f
        private var colorG: Float = 1.0f
        private var colorB: Float = 1.0f
        private var colorA: Float = 1.0f
        private var scale: Float = 1.0f

        fun position(x: Float = 0f, y: Float = 0f) {
            positionX = x
            positionY = y
        }

        fun scale(scale: Float = 1f) {
            this.scale = scale
        }

        fun color(r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f) {
            colorR = r
            colorG = g
            colorB = b
            colorA = a
        }

        fun build(spriteSheet: CSpriteSheet): TextElement {

            val cTextElement = memScoped {
                val info = cValue<CreateTextElementInfo> {
                    textTemplate = template.cstr.ptr
                    replacements = params.toTypedArray().toCStringArray(memScope)
                    numReplacements = params.size.toLong()
                    positionX = this@TextElementBuilder.positionX
                    positionY = this@TextElementBuilder.positionY
                    colorR = this@TextElementBuilder.colorR
                    colorG = this@TextElementBuilder.colorG
                    colorB = this@TextElementBuilder.colorB
                    colorA = this@TextElementBuilder.colorA
                    scale = this@TextElementBuilder.scale
                }
                c_createTextElement!!.invoke(spriteSheet, info.ptr)
            }

            return TextElement(
                elementIndex,
                spriteBatchHandle,
                cTextElement,
                positionX,
                positionY
            )

        }
    }
}

@OptIn(ExperimentalForeignApi::class)
class TextElement(
    val textBatchElementIndex: Int,
    val textBatchHandle: CTextBatch,
    val handle: CTextElement,
    initialPositionX: Float = 0.0f,
    initialPositionY: Float = 0.0f,
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

    fun setPosition(x: Float? = null, y: Float? = null) {
        _positionX = x ?: _positionX
        _positionY = y ?: _positionY
        c_updateTextElementPosition!!.invoke(
            ApplicationContext.vulcanContext!!,
            textBatchHandle,
            textBatchElementIndex,
            _positionX,
            _positionY
        )
    }

    fun updateSegment(segmentIndex: Int, newText: String) {
        memScoped {
            c_updateTextElementSegmentInBatch!!.invoke(
                ApplicationContext.vulcanContext!!,
                textBatchHandle,
                textBatchElementIndex,
                segmentIndex,
                newText.cstr.ptr
            )
        }
    }

    fun measureWidth(): Int {
        return c_measureTextElementWidth!!.invoke(handle)
    }

    fun measureHeight(): Int {
        return c_measureTextElementHeight!!.invoke(handle)
    }

    /**
     * Set the position of the text element to the center of the screen
     *
     * @param y The y position of the text element
     */
    fun centerX(y: Float? = null) {
        val width = measureWidth()
        setPosition(x = (getScreenWidth() / 2f) - (width / 2f), y = y)
    }

    fun centerY(x: Float? = null) {
        val height = measureHeight()
        setPosition(x = x, y = (getScreenWidth() / 2f) - (height / 2f))
    }

    fun center() {
        centerX()
        centerY()
    }
}

