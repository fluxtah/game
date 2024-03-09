package com.fluxtah.application.api.text

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.interop.CTextBatch
import com.fluxtah.application.api.interop.c_updateTextElementSegmentInBatch
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped

@OptIn(ExperimentalForeignApi::class)
class TextBatch(
    val id: String,
    val handle: CTextBatch,
    var visible: Boolean = true,
    private val elements: MutableList<TextElement> = mutableListOf()
) {
    fun elementAt(elementIndex: Int): TextElement = elements[elementIndex]
}