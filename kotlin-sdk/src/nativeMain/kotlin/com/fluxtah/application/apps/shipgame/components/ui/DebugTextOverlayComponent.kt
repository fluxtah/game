package com.fluxtah.application.apps.shipgame.components.ui

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.getScreenWidth
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.api.text.TextBatch
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class DebugTextOverlayComponent : SceneComponent() {
    private val textBatch: TextBatch by lazy { scene.textBatchById(Id.TEXT_BATCH_DEBUG_OVERLAY) }

    init {
        onBuildScene = {
            textBatch(Id.TEXT_BATCH_DEBUG_OVERLAY, spriteSheetId = Id.TEXT_SHEET_COURIER_NEW) {
                text("Ship Pos: %s %s %s", "     ", "     ", "     ") {
                    scale(0.3f)
                    color(1.0f, 1.0f, 1.0f, 0.5f)
                }
                text("Ship Vel: %s %s %s", "     ", "     ", "     ") {
                    color(1.0f, 1.0f, 1.0f, 0.5f)
                    scale(0.3f)
                }
                text("Time: %s", "        ") {
                    color(1.0f, 1.0f, 1.0f, 0.5f)
                    scale(0.3f)
                }
            }
        }
    }

    override fun initialize() {
        val pad = 16f
        textBatch.elementAt(0).apply {
            val width = measureWidth()
            setPosition(getScreenWidth() - width - pad, 150.0f)
        }

        textBatch.elementAt(1).apply {
            val width = measureWidth()
            setPosition(getScreenWidth() - width - pad, 180.0f)
        }

        textBatch.elementAt(2).apply {
            val width = measureWidth()
            setPosition(getScreenWidth() - width - pad, 240.0f)
        }
    }

    override fun onSceneUpdate(time: Float) {
        val playerShip = scene.entityInPoolByCondition(
            Id.ENT_PLAYER_SHIP,
            condition = { it.data<ShipData>().playerData.id == 0 })

        if (playerShip == null) {
            return
        }

        val playerShipData = playerShip.data<ShipData>()
        textBatch.elementAt(0).apply {
            updateSegment(0, "${playerShip.positionX}")
            updateSegment(1, "${playerShip.positionY}")
            updateSegment(2, "${playerShip.positionZ}")
        }

        textBatch.elementAt(1).apply {
            updateSegment(0, "${playerShipData.velocity.x}")
            updateSegment(1, "${playerShipData.velocity.y}")
            updateSegment(2, "${playerShipData.velocity.z}")
        }

        textBatch.elementAt(2).updateSegment(0, "$time")
    }
}

fun SceneBuilder.debugOverlayUiComponent() {
    component(Id.COMPONENT_DEBUG_OVERLAY_UI) {
        DebugTextOverlayComponent()
    }
}
