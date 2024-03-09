package com.fluxtah.application.apps.shipgame.scenes.main.sheets

import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id

fun SceneBuilder.spriteSheetGameUi() {
    spriteSheet(
        Id.SPRITE_SHEET_GAME_UI,
        "sheets/game-ui.json",
        "sheets/game-ui.png"
    )
}