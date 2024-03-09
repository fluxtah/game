package com.fluxtah.application.api.sprite

import com.fluxtah.application.api.ApplicationContext
import com.fluxtah.application.api.interop.c_createSpriteSheet
import com.fluxtah.application.api.interop.model.CreateSpriteSheetInfo
import com.fluxtah.application.api.scene.Scene
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped

class SpriteSheetBuilder(
    private val scene: Scene,
    private val id: String,
    private val jsonFileName: String,
    private val textureFileName: String
) {
    @OptIn(ExperimentalForeignApi::class)
    fun build(): SpriteSheet {
        val cSpriteSheet = memScoped {
            val info = cValue<CreateSpriteSheetInfo> {
                jsonFileName = this@SpriteSheetBuilder.jsonFileName.cstr.ptr
                textureFileName = this@SpriteSheetBuilder.textureFileName.cstr.ptr
            }
            c_createSpriteSheet!!.invoke(ApplicationContext.vulcanContext!!, info.ptr)
        }
        return SpriteSheet(id, cSpriteSheet)
    }
}