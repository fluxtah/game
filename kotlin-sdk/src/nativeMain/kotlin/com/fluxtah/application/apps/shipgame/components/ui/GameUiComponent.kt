package com.fluxtah.application.apps.shipgame.components.ui

import com.fluxtah.application.api.getScreenHeight
import com.fluxtah.application.api.getScreenWidth
import com.fluxtah.application.api.interop.getWorldToScreenPoint
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.api.sprite.SpriteBatch
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

class GameUiComponent : SceneComponent() {
    private var time: Float = 0f
    private val uiSprites: SpriteBatch by lazy { scene.spriteBatchById(Id.SPRITE_BATCH_GAME_UI)!! }
    private val sceneData by lazy { scene.data<GameData>() }
    private val gameTextBatch by lazy { scene.textBatchById(Id.TEXT_BATCH_GAME_UI) }

    private val notificationData: MutableList<NotificationData> = mutableListOf()
    private val notificationElementStartIndex: Int = 1
    private val notificationElementEndIndex: Int = 9

    var playerShipData: ShipData? = null

    init {
        onBuildScene = {
            textBatch(Id.TEXT_BATCH_GAME_UI, spriteSheetId = Id.TEXT_SHEET_COURIER_NEW) {
                text("%s", "00:00") {
                    scale(0.5f)
                }

                for (i in 0 until 8) {
                    text("%s", "                                    ") {
                        position(16.0f, 300 + (i * 50f))
                        scale(0.3f)
                    }
                }

                text("%s", "---0") {
                    color(0.0f, 1.0f, 0.0f, 1.0f)
                    scale(0.3f)
                }
                text("%s", "0---") {
                    color(1.0f, 0.0f, 0.0f, 1.0f)
                    scale(0.3f)
                }

                text("%s", "        ") {
                    scale(0.2f)
                    color(1.0f, 0.0f, 0.0f, 1.0f)
                }
            }

            spriteBatch(Id.SPRITE_BATCH_GAME_UI, spriteSheetId = Id.SPRITE_SHEET_GAME_UI) {
                sprite(0) {
                    position(32.0f, getScreenHeight() - 180f)
                    rotation(0.0f)
                    scale(1.0f)
                }
                sprite(1) {
                    position(32.0f, getScreenHeight() - 130f)
                    rotation(0.0f)
                    scale(1.0f)
                }
                sprite(2) {
                    position(32.0f, getScreenHeight() - 80f)
                    rotation(0.0f)
                    scale(1.0f)
                }

                sprite(1) {
                    scale(0.2f)
                }
            }
        }
    }

    override fun initialize() {
        deactivate()

        val pad = 32
        val screenWidth = getScreenWidth()
        val screenCenterX = screenWidth / 2
        val timeElement = gameTextBatch.elementAt(0)
        val timeElementWidth = timeElement.measureWidth()

        timeElement.setPosition((screenCenterX - (timeElementWidth / 2)).toFloat(), 32.0f)

        gameTextBatch.elementAt(9).apply {
            val width = measureWidth()
            setPosition((screenCenterX - width - (timeElementWidth/2) - pad).toFloat(), 32.0f)
        }

        gameTextBatch.elementAt(10).apply {
            val width = measureWidth()
            setPosition((screenCenterX + (timeElementWidth / 2) + pad).toFloat(), 32.0f)
        }
    }

    override fun onBeforeSceneUpdate(time: Float, deltaTime: Float) {
        this.time = time
        val playerShipData = playerShipData ?: return

        if (sceneData.gameTimeRemaining <= 0) {
            return
        }

        val shield = 1f / 100f * playerShipData.shield % 101f
        uiSprites.elementAt(SpriteElementIds.ELEMENT_SHIELD).cropWidth(0f, shield)

        val armor = 1f / 100f * playerShipData.armor % 101f
        uiSprites.elementAt(SpriteElementIds.ELEMENT_ARMOR).cropWidth(0f, armor)

        val energy = 1f / 100f * playerShipData.energy % 101f
        uiSprites.elementAt(SpriteElementIds.ELEMENT_ENERGY).cropWidth(0f, energy)

        val minutes = (sceneData.gameTimeRemaining / 60).toInt().toString().padStart(2, '0')
        val seconds = (sceneData.gameTimeRemaining % 60).toInt().toString().padStart(2, '0')
        gameTextBatch.elementAt(0).updateSegment(0, "$minutes:$seconds")

        if (notificationData.isNotEmpty()) {
            val notification = notificationData.first()
            if (time - notification.time > notification.expiresIn) {
                notificationData.removeAt(0)
                gameTextBatch.elementAt(notification.elementIndex).updateSegment(0, "")
            }
        }

        gameTextBatch.elementAt(9).updateSegment(0, "${sceneData.friendlyTeam.score.toInt()}".padStart(4, ' '))
        gameTextBatch.elementAt(10).updateSegment(0, "${sceneData.enemyTeam.score.toInt()}".padEnd(4, ' '))

        val firstEnemyShip = scene.entitiesInPool(Id.ENT_PLAYER_SHIP).first { it.data<ShipData>().playerData.team != playerShipData.playerData.team }
        val screenPoint = getWorldToScreenPoint(firstEnemyShip.positionX, firstEnemyShip.positionY, firstEnemyShip.positionZ)
        gameTextBatch.elementAt(11).updateSegment(0, firstEnemyShip.data<ShipData>().playerData.name)
        val width = gameTextBatch.elementAt(11).measureWidth()
        gameTextBatch.elementAt(11).setPosition(screenPoint.x - (width/2), screenPoint.y - 64)

        val enemyArmor = 1f / 100f * firstEnemyShip.data<ShipData>().armor % 101f
        uiSprites.elementAt(3).cropWidth(0f, enemyArmor)
        uiSprites.elementAt(3).position(screenPoint.x - (width/2), screenPoint.y - 32)
    }

    fun addNotification(text: String) {
        println("Adding notification: $text")
        var elementIndex = notificationData.size + notificationElementStartIndex
        if (elementIndex > notificationElementEndIndex) {
            notificationData.removeAt(0)
        }
        elementIndex = notificationData.size + notificationElementStartIndex
        notificationData.add(NotificationData(text, time, elementIndex))
        gameTextBatch.elementAt(elementIndex).updateSegment(0, text)
    }

    override fun activate() {
        uiSprites.visible = true
        gameTextBatch.visible = true
    }

    override fun deactivate() {
        uiSprites.visible = false
        gameTextBatch.visible = false
    }
}

fun SceneBuilder.gameUiComponent() {
    component(Id.COMPONENT_GAME_UI) {
        GameUiComponent()
    }
}

object SpriteElementIds {
    const val ELEMENT_SHIELD = 0
    const val ELEMENT_ARMOR = 1
    const val ELEMENT_ENERGY = 2
}

data class NotificationData(
    val text: String,
    val time: Float,
    val elementIndex: Int,
    val expiresIn: Float = 5f
)