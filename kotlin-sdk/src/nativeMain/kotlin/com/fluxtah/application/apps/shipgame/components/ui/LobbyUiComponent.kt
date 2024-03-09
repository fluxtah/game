package com.fluxtah.application.apps.shipgame.components.ui

import com.fluxtah.application.api.getScreenWidth
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame.Companion.PLAYERS_PER_TEAM

class LobbyUiComponent : SceneComponent() {
    private val lobbyTextBatch by lazy { scene.textBatchById(Id.TEXT_BATCH_LOBBY_UI) }
    private val data by lazy { scene.data<GameData>() }

    init {
        onBuildScene = {
            textBatch(Id.TEXT_BATCH_LOBBY_UI, spriteSheetId = Id.TEXT_SHEET_COURIER_NEW) {
                text("MATCH STARTS %s", "00:00") {
                    scale(0.7f)
                }

                text("YOUR TEAM") {
                    position(32.0f, 140.0f)
                    scale(0.7f)
                }
                for (i in 0 until PLAYERS_PER_TEAM) {
                    text("${i + 1}. %s", "................") {
                        position(32.0f, 170.0f + (i + 1) * 64.0f)
                        scale(0.4f)
                    }
                }

                text("ENEMY TEAM") {
                    position(600.0f, 140.0f)
                    scale(0.7f)
                }
                for (i in 0 until PLAYERS_PER_TEAM) {
                    text("${i + 1}. %s", "................") {
                        position(600.0f, 170.0f + (i + 1) * 64.0f)
                        scale(0.4f)
                    }
                }
            }
        }
    }

    override fun initialize() {
        deactivate()

        lobbyTextBatch.elementAt(0).centerX(y = 32f)

        val colWidth = lobbyTextBatch.elementAt(2).measureWidth()
        val pad = 64.0f
        val rowHeight = 80.0f
        val headerTop = 200.0f
        val firstRowTop = 230.0f

        lobbyTextBatch.elementAt(1)
            .setPosition(getScreenWidth() / 2f - colWidth - pad, headerTop)
        for (i in 0 until PLAYERS_PER_TEAM) {
            lobbyTextBatch.elementAt(i + 2)
                .setPosition(getScreenWidth() / 2f - colWidth - pad, firstRowTop + (i + 1) * rowHeight)
        }

        lobbyTextBatch.elementAt(PLAYERS_PER_TEAM + 2)
            .setPosition(getScreenWidth() / 2f + pad, headerTop)
        for (i in 0 until PLAYERS_PER_TEAM) {
            lobbyTextBatch.elementAt(i + 2 + PLAYERS_PER_TEAM + 1)
                .setPosition(getScreenWidth() / 2f + pad, firstRowTop + (i + 1) * rowHeight)
        }

    }

    override fun activate() {
        lobbyTextBatch.visible = true
    }

    override fun deactivate() {
        lobbyTextBatch.visible = false
    }

    override fun onBeforeSceneUpdate(time: Float, deltaTime: Float) {
        val minutes = (data.lobbyTimeRemaining / 60).toInt().toString().padStart(2, '0')
        val seconds = (data.lobbyTimeRemaining % 60).toInt().toString().padStart(2, '0')
        lobbyTextBatch.elementAt(0).updateSegment(0, "$minutes:$seconds")

        for (i in 0 until PLAYERS_PER_TEAM) {
            val player = data.friendlyTeam.players[i]
            lobbyTextBatch.elementAt(i + 2).updateSegment(0, player.name)
        }

        for (i in 0 until PLAYERS_PER_TEAM) {
            val player = data.enemyTeam.players[i]
            lobbyTextBatch.elementAt(i + 2 + PLAYERS_PER_TEAM + 1).updateSegment(0, player.name)
        }
    }
}


fun SceneBuilder.lobbyUiComponent() {
    component(Id.COMPONENT_LOBBY_UI) {
        LobbyUiComponent()
    }
}
