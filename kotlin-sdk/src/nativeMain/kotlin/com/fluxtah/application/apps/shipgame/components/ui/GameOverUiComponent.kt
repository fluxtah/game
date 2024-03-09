package com.fluxtah.application.apps.shipgame.components.ui

import com.fluxtah.application.api.getScreenWidth
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame

class GameOverUiComponent : SceneComponent() {
    val gameOverTextBatch by lazy { scene.textBatchById(Id.TEXT_BATCH_GAME_OVER_UI) }
    val data by lazy { scene.data<GameData>() }

    init {
        onBuildScene = {
            textBatch(Id.TEXT_BATCH_GAME_OVER_UI, spriteSheetId = Id.TEXT_SHEET_COURIER_NEW) {
                text("%s %s", "YOUR TEAM WON", "00:00") {
                    position(32.0f, 32.0f)
                    scale(0.7f)
                }

                text("TEAM 1") {
                    position(32.0f, 140.0f)
                    scale(0.7f)
                }
                for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
                    text("${i + 1}. %s %s", "................", "00000000") {
                        position(32.0f, 170.0f + (i + 1) * 64.0f)
                        scale(0.4f)
                    }
                }

                text("TEAM 2") {
                    position(900.0f, 140.0f)
                    scale(0.7f)
                }
                for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
                    text("${i + 1}. %s %s", "................", "00000000") {
                        position(900.0f, 170.0f + (i + 1) * 64.0f)
                        scale(0.4f)
                    }
                }
            }
        }
    }

    override fun initialize() {
        deactivate()

        gameOverTextBatch.elementAt(0).centerX(y = 32f)

        val colWidth = gameOverTextBatch.elementAt(2).measureWidth()
        val pad = 64.0f
        val rowHeight = 80.0f
        val headerTop = 200.0f
        val firstRowTop = 230.0f

        gameOverTextBatch.elementAt(1)
            .setPosition(getScreenWidth() / 2f - colWidth - pad, headerTop)
        for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
            gameOverTextBatch.elementAt(i + 2)
                .setPosition(getScreenWidth() / 2f - colWidth - pad, firstRowTop + (i + 1) * rowHeight)
        }

        gameOverTextBatch.elementAt(ShipGame.PLAYERS_PER_TEAM + 2)
            .setPosition(getScreenWidth() / 2f + pad, headerTop)
        for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
            gameOverTextBatch.elementAt(i + 2 + ShipGame.PLAYERS_PER_TEAM + 1)
                .setPosition(getScreenWidth() / 2f + pad, firstRowTop + (i + 1) * rowHeight)
        }
    }

    override fun activate() {
        gameOverTextBatch.visible = true
    }

    override fun deactivate() {
        gameOverTextBatch.visible = false
    }

    override fun onBeforeSceneUpdate(time: Float, deltaTime: Float) {
        val minutes = (data.lobbyTimeRemaining / 60).toInt().toString().padStart(2, '0')
        val seconds = (data.lobbyTimeRemaining % 60).toInt().toString().padStart(2, '0')
        if (data.enemyTeam.score > data.friendlyTeam.score) {
            gameOverTextBatch.elementAt(0).updateSegment(0, "ENEMY TEAM WON")
        } else if (data.enemyTeam.score < data.friendlyTeam.score) {
            gameOverTextBatch.elementAt(0).updateSegment(0, "YOUR TEAM WON")
        } else {
            gameOverTextBatch.elementAt(0).updateSegment(0, "DRAW")
        }
        gameOverTextBatch.elementAt(0).updateSegment(1, "$minutes:$seconds")

        for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
            val player = data.friendlyTeam.players[i]
            gameOverTextBatch.elementAt(i + 2).updateSegment(0, player.name)
        }

        for (i in 0 until ShipGame.PLAYERS_PER_TEAM) {
            val player = data.enemyTeam.players[i]
            gameOverTextBatch.elementAt(i + 2 + ShipGame.PLAYERS_PER_TEAM + 1).updateSegment(0, player.name)
        }
    }
}

fun SceneBuilder.gameOverUiComponent() {
    component(Id.COMPONENT_GAME_OVER_UI) {
        GameOverUiComponent()
    }
}
