package com.fluxtah.application.apps.shipgame.scenes

import com.fluxtah.application.api.Application
import com.fluxtah.application.api.collision.CollisionHandler
import com.fluxtah.application.api.fixedTimeStep
import com.fluxtah.application.api.scene.scene
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.components.map.mapComponent
import com.fluxtah.application.apps.shipgame.components.playerInputComponent
import com.fluxtah.application.apps.shipgame.components.ui.gameOverUiComponent
import com.fluxtah.application.apps.shipgame.components.ui.gameUiComponent
import com.fluxtah.application.apps.shipgame.components.ui.lobbyUiComponent
import com.fluxtah.application.apps.shipgame.handleCameraInput
import com.fluxtah.application.apps.shipgame.handleInput
import com.fluxtah.application.apps.shipgame.logic.initPlayers
import com.fluxtah.application.apps.shipgame.scenes.main.cameraOne
import com.fluxtah.application.apps.shipgame.scenes.main.cameraTwo
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ProjectileToBlockCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ProjectileToDestroyableCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ProjectileToPlayerShipCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ShipToBlockCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ShipToDestroyableCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.collision.ShipToShipCollisionHandler
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.emitters.emitterPoolExplosion
import com.fluxtah.application.apps.shipgame.scenes.main.emitters.emitterPoolSmoke
import com.fluxtah.application.apps.shipgame.scenes.main.entities.entityPoolAsteroid
import com.fluxtah.application.apps.shipgame.scenes.main.entities.entityPoolPlayerPlasmaBolt
import com.fluxtah.application.apps.shipgame.scenes.main.entities.playership.entityPoolPlayerShip
import com.fluxtah.application.apps.shipgame.scenes.main.killerCamera
import com.fluxtah.application.apps.shipgame.scenes.main.lightOne
import com.fluxtah.application.apps.shipgame.scenes.main.sequences.sequenceGameState
import com.fluxtah.application.apps.shipgame.scenes.main.sequences.sequencePlayerDeathRespawn
import com.fluxtah.application.apps.shipgame.scenes.main.sheets.spriteSheetCourierNew
import com.fluxtah.application.apps.shipgame.scenes.main.sheets.spriteSheetGameUi
import com.fluxtah.application.apps.shipgame.scenes.main.sounds.shipSounds
import kotlin.math.sin

fun Application.mainScene() {
    scene(Id.SCENE_MAIN) {
        data { GameData() }
        lobbyUiComponent()
        gameUiComponent()
        gameOverUiComponent()
        // debugOverlayUiComponent()

        mapComponent()

        playerInputComponent()

        spriteSheetCourierNew()
        spriteSheetGameUi()

        cameraOne()
        cameraTwo()
        killerCamera()

        lightOne()
        // lightTwo()

        entityPoolPlayerShip()

        sequenceGameState()
        sequencePlayerDeathRespawn()

        entityPoolPlayerPlasmaBolt()

        entityPoolAsteroid()

        emitterPoolExplosion()

        emitterPoolSmoke()

//        entity("ring", "models/energy-ring/energy-ring.glb") {
//            position(0f, -3.7f, 0f)
//            scale(10f, 10f, 10f)
//            skin(0)
//        }
//        entity("ring2", "models/energy-ring/energy-ring.glb") {
//            position(0f, -3.71f, 0f)
//            scale(10f, 10f, 10f)
//            skin(1)
//        }

        shipSounds()

        onSceneCreated { scene ->
            scene.setActiveCamera(Id.CAMERA2)
            initPlayers(scene.data())
            scene.createSequence(Id.SEQ_GAME_STATE)!!.play()
        }

        onBeforeSceneUpdate { scene, time, _ ->
//            val scaleFrom = 1.0f
//            val scaleTo = 100.0f
//            // calcualte scale based on time between scaleFrom and scaleTo
//            val scale = scaleFrom + (sin(time) * (scaleTo - scaleFrom))
//            val rotate = time * 0.01f
//
//            val ring = scene.entityById("ring")
//            ring.setScale(scale, 1f, scale)
//            ring.setSkin(1)
//            ring.rotate(y = rotate)
//
//            val ring2 = scene.entityById("ring2")
//            ring2.setScale(scaleTo - scale, 1f, scaleTo - scale)
//            ring2.setSkin(1)
//            ring2.rotate(y = -rotate * 0.8f)

            handleInput(scene)
        }
        onSceneUpdate { scene, _ ->
            if (scene.activeCamera() == scene.cameraById(Id.CAMERA2)) {
                handleCameraInput(scene, fixedTimeStep)
            }
        }

        val collisionHandlers = mutableListOf<CollisionHandler>()
        collisionHandlers.add(ProjectileToPlayerShipCollisionHandler())
        collisionHandlers.add(ProjectileToBlockCollisionHandler())
        collisionHandlers.add(ProjectileToDestroyableCollisionHandler())
        collisionHandlers.add(ShipToBlockCollisionHandler())
        collisionHandlers.add(ShipToDestroyableCollisionHandler())
        collisionHandlers.add(ShipToShipCollisionHandler())

        onCollision { scene, result ->
            for (handler in collisionHandlers) {
                if (handler.handleCollision(scene, result)) {
                    return@onCollision
                }
            }
        }
    }
}

