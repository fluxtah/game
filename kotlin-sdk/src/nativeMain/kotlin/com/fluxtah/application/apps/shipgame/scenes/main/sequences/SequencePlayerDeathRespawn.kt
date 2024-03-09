package com.fluxtah.application.apps.shipgame.scenes.main.sequences

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameSceneState
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.behaviors.ForwardMovementBehavior
import com.fluxtah.application.apps.shipgame.behaviors.KillerCameraBehavior
import com.fluxtah.application.apps.shipgame.components.map.MapComponent
import com.fluxtah.application.apps.shipgame.scenes.main.data.ShipData

fun SceneBuilder.sequencePlayerDeathRespawn() {
    sequence(Id.SEQ_DEATH_RESPAWN) {
        wait(duration = 2f)
        action { scene, sequenceArgs ->
            val args = sequenceArgs as SequencePlayerDeathRespawnArgs

            if (args.ship.data<ShipData>().playerData.isLocalPlayer() && args.killer != null) {
                scene.componentById<MapComponent>(Id.COMPONENT_MAP).originEntity = args.killer
                scene.cameraById(Id.KILLER_CAMERA)?.apply {
                    lookAt(args.killer.positionX, args.killer.positionY, args.killer.positionZ)
                    behaviors.filterIsInstance<KillerCameraBehavior>().first().apply {
                        target = args.killer
                        position.update(args.ship.positionX, args.ship.positionY, args.ship.positionZ)
                    }
                }
                scene.setActiveCamera(Id.KILLER_CAMERA)
            }
        }
        wait(duration = 10f)
        action { scene, sequenceArgs ->
            if (scene.data<GameData>().sceneState == GameSceneState.Playing) {
                val args = sequenceArgs as SequencePlayerDeathRespawnArgs
                val startPos = args.ship.data<ShipData>().playerData.startPos
                args.ship.setPosition(startPos.x, startPos.y, startPos.z)
                args.ship.data<ShipData>().resetAll()
                args.ship.resetBehaviors()
                args.ship.visible = true
                args.ship.active = true
                args.ship.getBehaviorByType<ForwardMovementBehavior>().startEngine()

                if (args.ship.data<ShipData>().playerData.isLocalPlayer()) {
                    scene.componentById<MapComponent>(Id.COMPONENT_MAP).originEntity = args.ship
                    scene.setActiveCamera(Id.CAMERA1)
                }
            }
        }
    }
}

data class SequencePlayerDeathRespawnArgs(
    val killer: Entity? = null,
    val ship: Entity
)