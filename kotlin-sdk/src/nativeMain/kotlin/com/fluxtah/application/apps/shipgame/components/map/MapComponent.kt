package com.fluxtah.application.apps.shipgame.components.map

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.SceneBuilder
import com.fluxtah.application.api.scene.SceneComponent
import com.fluxtah.application.apps.shipgame.CollisionGroups
import com.fluxtah.application.apps.shipgame.Id
import com.fluxtah.application.apps.shipgame.ShipGame
import com.fluxtah.application.apps.shipgame.behaviors.PowerNodeDeathBehavior
import com.fluxtah.application.apps.shipgame.behaviors.PowerNodeSmokeBehavior
import com.fluxtah.application.apps.shipgame.behaviors.RechargeNearbyBehaviour
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PowerNodeData
import com.fluxtah.application.apps.shipgame.scenes.main.data.Team

class MapComponent : SceneComponent() {
    val sceneData by lazy { scene.data<GameData>() }

    data class TileData(
        val isPowerNode: Boolean = true,
        var isPowerNodePlaced: Boolean = false
    )

    private val mapHeight = InfiniteTileMap<TileData>(2, tileSize = 60)
    private val mapTexture = InfiniteTileMap<Int>(3, tileSize = 60)

    var originEntity: Entity? = null

    init {
        onBuildScene = {
            entityPool(ENTITY_POOL_BLOCK_DEFAULT, "models/blocks/default/block-default.glb") {
                mass(0f)
                initialSize(200)
                collisionGroup(CollisionGroups.GROUP_MAP_BLOCK)
                collisionMask(CollisionGroups.MASK_MAP_BLOCK)
            }
            entityPool(ENTITY_POOL_BLOCK_POWER, "models/blocks/power/block-power.glb") {
                mass(0f)
                data { PowerNodeData() }
                initialSize(ShipGame.PLAYERS_PER_TEAM * 10)
                collisionGroup(CollisionGroups.GROUP_MAP_BLOCK)
                collisionMask(CollisionGroups.MASK_MAP_BLOCK)

                behaviour { PowerNodeDeathBehavior() }
                behaviour { RechargeNearbyBehaviour() }
                behaviour { PowerNodeSmokeBehavior() }
            }
        }
    }

    fun clearDataAtWorldPosition(posX: Float, posZ: Float) {
        mapHeight.clearTileDataByWorldPosition(posX, posZ)
    }

    fun placePowerNode(posX: Float, posZ: Float, team: Team): Boolean {
        val existingData = mapHeight.getTileDataByWorldPosition(posX, posZ)

        // Don't place a power node on top of an existing power node
        if (existingData != null && existingData.isPowerNode) {
            return false
        }

        val tile = mapHeight.getTileByWorldPosition(posX, posZ)

        scene.entityFromPool(ENTITY_POOL_BLOCK_POWER) { powerNodeEntity ->
            powerNodeEntity.setPosition(tile.worldX.toFloat(), -4.0f, tile.worldZ.toFloat())
            powerNodeEntity.updatePhysicsState()
            val powerNodeData = powerNodeEntity.data<PowerNodeData>()
            powerNodeData.resetAll()
            powerNodeData.team = team
            powerNodeEntity.visible = true

            if (team == Team.Friendly) {
                powerNodeEntity.setSkin(sceneData.friendlyTeam.skin)
            } else {
                powerNodeEntity.setSkin(sceneData.enemyTeam.skin)
            }
        }

        mapHeight.associateTileDataByWorldPosition(posX, posZ, TileData(isPowerNode = true))

        return true
    }

    fun getTileByWorldPosition(posX: Float, posZ: Float) = mapHeight.getTileByWorldPosition(posX, posZ)

    override fun onBeforeSceneUpdate(time: Float, deltaTime: Float) {
        layoutTiles()
    }

    private fun layoutTiles() {
        val originEntity = originEntity ?: return

        scene.resetEntityPool(ENTITY_POOL_BLOCK_DEFAULT)
        val textureTiles = mapTexture.getTilesByWorldPosition(originEntity.positionX, originEntity.positionZ, 6)
        mapHeight.getTilesByWorldPosition(originEntity.positionX, originEntity.positionZ, 6)
            .forEachIndexed { index, tile ->
                val tileData = mapHeight.getTileData(tile.gridX, tile.gridZ)
                if (tileData == null) {
                    val texTile = textureTiles[index]

                    val skin = when (texTile.type) {
                        TileType.LEVEL1 -> 1
                        TileType.LEVEL2 -> 2
                        TileType.LEVEL3 -> 3
                        TileType.LEVEL4 -> 4
                        TileType.LEVEL5 -> 3
                        TileType.LEVEL6 -> 1
                    }
                    scene.entityFromPool(ENTITY_POOL_BLOCK_DEFAULT) { tileEntity ->
                        tileEntity.setSkin(skin)
                        when (tile.type) {
                            TileType.LEVEL1 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), -10.0f, tile.worldZ.toFloat())
                            }

                            TileType.LEVEL2 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), -8.0f, tile.worldZ.toFloat())
                            }

                            TileType.LEVEL3 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), -6.0f, tile.worldZ.toFloat())
                            }

                            TileType.LEVEL4 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), -4.0f, tile.worldZ.toFloat())
                            }

                            TileType.LEVEL5 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), -2.0f, tile.worldZ.toFloat())
                            }

                            TileType.LEVEL6 -> {
                                tileEntity.setPosition(tile.worldX.toFloat(), 5.0f, tile.worldZ.toFloat())
                            }
                        }
                        tileEntity.updatePhysicsState()
                    }
                }
            }
    }

    fun clearTileData() {
        scene.resetEntityPool(ENTITY_POOL_BLOCK_POWER)
        mapHeight.clearTileData()
    }

    companion object {
        const val ENTITY_POOL_BLOCK_DEFAULT = "map-block-default"
        const val ENTITY_POOL_BLOCK_POWER = "map-block-power"
    }
}

fun SceneBuilder.mapComponent() {
    component(Id.COMPONENT_MAP) {
        MapComponent()
    }
}


