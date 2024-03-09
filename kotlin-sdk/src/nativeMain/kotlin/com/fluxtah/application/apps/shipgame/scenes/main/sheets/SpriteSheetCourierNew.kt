package com.fluxtah.application.apps.shipgame.scenes.main.sheets

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.spriteSheetCourierNew() {
    spriteSheet(
        id = Id.TEXT_SHEET_COURIER_NEW,
        jsonFilePath = "sheets/courier-new.json",
        textureFilePath = "sheets/courier-new.png"
    )
}
