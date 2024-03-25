package com.fluxtah.application.apps.shipgame.scenes.main.sequences

import com.fluxtah.application.api.camera.Camera
import com.fluxtah.application.api.math.toRadians
import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame
import com.fluxtah.application.apps.shipgame.behaviors.ChaseCameraBehavior
import com.fluxtah.application.apps.shipgame.components.PlayerInputComponent
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.components.ui.GameUiComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameSceneState
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

private const val LOBBY_TIME_MS = 10f
private const val GAME_TIME_MS = 60f * 5f
private const val GAME_OVER_TIME_MS = 10f

fun SceneBuilder.sequenceGameState() {

    sequence(Id.SEQ_GAME_STATE) {
        //
        // Lobby sequence
        //
        setCamera(Id.CAMERA1)
        deactivateComponent(Id.COMPONENT_GAME_OVER_UI)
        activateComponent(Id.COMPONENT_LOBBY_UI)
        onComponent<MapComponent>(Id.COMPONENT_MAP) { _, map, _ -> map.clearTileData() }
        onSceneData<GameData> { _, _ ->
            sceneState = GameSceneState.Lobby
            enemyTeam.score = 0f
            friendlyTeam.score = 0f
            friendlyTeam.placePowerNodeCoolDownTimer = placePowerNodeCoolDown
            enemyTeam.placePowerNodeCoolDownTimer = placePowerNodeCoolDown
        }
        action { scene, _ ->
            val camera = scene.cameraById(Id.CAMERA1)!!
            val sceneData = scene.data<GameData>()

            val teamPlayerDistanceFromCenter = (60 * 8) + 30f

            setupFriendlyPlayerEntities(sceneData, scene, teamPlayerDistanceFromCenter)
            setupEnemyPlayerEntities(sceneData, scene, teamPlayerDistanceFromCenter)
            setupLocalPlayerEntity(scene, camera)

            scene.resetEmitterPool(Id.EMITTER_EXPLOSION)
            scene.resetEmitterPool(Id.EMITTER_SMOKE)
        }
        wait(duration = LOBBY_TIME_MS) {
            val data = scene.data<GameData>()
            data.lobbyTimeRemaining = timeRemaining
        }

        //
        // Playing sequence
        //
        deactivateComponent(Id.COMPONENT_LOBBY_UI)
        activateComponent(Id.COMPONENT_GAME_UI)
        setCamera(Id.CAMERA1)
        action { scene, _ ->
            scene.apply {
                data<GameData>().sceneState = GameSceneState.Playing
                scene.data<GameData>().aiShipsEnabled = true
            }
        }

        wait(duration = GAME_TIME_MS) {
            val data = scene.data<GameData>()
            data.gameTimeRemaining = timeRemaining

            data.friendlyTeam.placePowerNodeCoolDownTimer += deltaTime
            data.enemyTeam.placePowerNodeCoolDownTimer += deltaTime

            if (data.friendlyTeam.score > ShipGame.TEAM_WIN_POWER_LEVEL
                || data.enemyTeam.score > ShipGame.TEAM_WIN_POWER_LEVEL
            ) {
                data.gameTimeRemaining = 0f
                finish()
            }
        }

        //
        // Game over sequence
        //
        deactivateComponent(Id.COMPONENT_GAME_UI)
        activateComponent(Id.COMPONENT_GAME_OVER_UI)
        onSceneData<GameData> { _, _ ->
            sceneState = GameSceneState.GameOver
            aiShipsEnabled = false
            gameTimeRemaining = 0f
        }
        resetEntityPool(Id.ENT_PLAYER_SHIP)
        wait(duration = GAME_OVER_TIME_MS) {
            val data = scene.data<GameData>()
            data.lobbyTimeRemaining = timeRemaining
        }

        // Restart the sequence
        startSequence(Id.SEQ_GAME_STATE)
    }
}

private fun setupLocalPlayerEntity(scene: Scene, camera: Camera) {
    scene.entityPool(Id.ENT_PLAYER_SHIP).entities.first { it.entity.data<ShipData>().playerData.isLocalPlayer() }
        .let { entityInfo ->
            val entity = entityInfo.entity
            camera.behaviors.filterIsInstance<ChaseCameraBehavior>().first().apply {
                target = entity
            }
            scene.componentById<GameUiComponent>(Id.COMPONENT_GAME_UI).playerShipData = entity.data()
            scene.componentById<MapComponent>(Id.COMPONENT_MAP).originEntity = entity
            scene.componentById<PlayerInputComponent>(Id.COMPONENT_PLAYER_INPUT).playerShipData = entity.data()
        }
}

private fun setupEnemyPlayerEntities(sceneData: GameData, scene: Scene, distanceFromCenter: Float) {
    sceneData.enemyTeam.players.forEachIndexed { index, playerData ->
        scene.entityFromPool(Id.ENT_PLAYER_SHIP) { entity ->
            entity.data<ShipData>().playerData = playerData
            entity.resetBehaviors()
            entity.data<ShipData>().resetAll()
            entity.visible = true
            entity.setPosition((index * 60f) - 30f, 800f, distanceFromCenter)
            playerData.startPos.update(
                entity.positionX,
                entity.positionY,
                entity.positionZ
            )
            // UNDONE: Bullet physics
            //entity.setRotation(0f, 180f.toRadians(), 0f)
            entity.setSkin(sceneData.enemyTeam.skin)
        }
    }
}

private fun setupFriendlyPlayerEntities(sceneData: GameData, scene: Scene, distanceFromCenter: Float) {
    sceneData.friendlyTeam.players.forEachIndexed { index, playerData ->
        scene.entityFromPool(Id.ENT_PLAYER_SHIP) { entity ->
            entity.data<ShipData>().playerData = playerData
            entity.resetBehaviors()
            entity.data<ShipData>().resetAll()
            entity.visible = true
            entity.setPosition((index * 60f) - 30f, 800f, -distanceFromCenter)
            playerData.startPos.update(
                entity.positionX,
                entity.positionY,
                entity.positionZ
            )

            // UNDONE: Bullet physics
            //entity.setRotation(0f, 0f, 0f)
            entity.setSkin(sceneData.friendlyTeam.skin)
        }
    }
}
