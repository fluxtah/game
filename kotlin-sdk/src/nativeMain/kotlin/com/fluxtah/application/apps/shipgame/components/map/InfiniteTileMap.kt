package com.fluxtah.application.apps.shipgame.components.map

import kotlin.math.floor


data class TileKey(val x: Int, val y: Int)
class InfiniteTileMap<DATA>(seed: Int, val tileSize: Int = 20) {
    private val noise = PerlinNoise(seed)  // Assuming PerlinNoise is a properly implemented class

    private val mapData = HashMap<TileKey, DATA>()

    private fun associateTileData(x: Int, y: Int, data: DATA) {
        mapData[TileKey(x, y)] = data
    }

    fun associateTileDataByWorldPosition(posX: Float, posZ: Float, data: DATA) {
        val playerTileX = floor(posX / tileSize).toInt()
        val playerTileZ = floor(posZ / tileSize).toInt()

        associateTileData(playerTileX, playerTileZ, data)
    }

    fun getTileData(x: Int, y: Int): DATA? {
        return mapData[TileKey(x, y)]
    }

    fun getTileDataByWorldPosition(posX: Float, posZ: Float): DATA? {
        val playerTileX = floor(posX / tileSize).toInt()
        val playerTileZ = floor(posZ / tileSize).toInt()

        return getTileData(playerTileX, playerTileZ)
    }

    fun getTile(gridX: Int, gridZ: Int): Tile {
        val noiseValue = noise.generate(gridX.toDouble() * 1.2424, gridZ.toDouble() * 1.2424)
        return Tile(gridX, gridZ, determineTileType(noiseValue), tileSize)
    }

    private fun determineTileType(noiseValue: Double): TileType {
        val tileType: Int = (((noiseValue - 0.2) * 1.6) * 10).toInt() // range from 0 to 9
        return if (tileType < 2) {
            TileType.LEVEL1
        } else if (tileType < 3) {
            TileType.LEVEL2
        } else if (tileType < 4) {
            TileType.LEVEL3
        } else if (tileType < 5) {
            TileType.LEVEL4
        } else if (tileType < 8) {
            TileType.LEVEL3
        } else {
            TileType.LEVEL6
        }
    }

    fun getTileByWorldPosition(posX: Float, posZ: Float): Tile {
        val playerTileX = floor(posX / tileSize).toInt()
        val playerTileZ = floor(posZ / tileSize).toInt()

        return getTile(playerTileX, playerTileZ)
    }

    fun getTilesByWorldPosition(posX: Float, posZ: Float, range: Int): List<Tile> {
        val playerTileX = floor(posX / tileSize).toInt()
        val playerTileZ = floor(posZ / tileSize).toInt()

        return generateSurroundingTiles(playerTileX, playerTileZ, range)
    }

    private fun generateSurroundingTiles(centerX: Int, centerZ: Int, range: Int): List<Tile> {
        val tiles = mutableListOf<Tile>()
        for (x in (centerX - range)..(centerX + range)) {
            for (z in (centerZ - range)..(centerZ + range)) {
                tiles.add(getTile(x, z))
            }
        }
        return tiles
    }

    fun clearTileData() {
        mapData.clear()
    }

    fun clearTileDataByWorldPosition(posX: Float, posZ: Float) {
        val playerTileX = floor(posX / tileSize).toInt()
        val playerTileZ = floor(posZ / tileSize).toInt()

        mapData.remove(TileKey(playerTileX, playerTileZ))
    }
}

enum class TileType {
    LEVEL1,
    LEVEL2,
    LEVEL3,
    LEVEL4,
    LEVEL5,
    LEVEL6,
}

data class Tile(val gridX: Int, val gridZ: Int, val type: TileType, val tileSize: Int) {
    // Calculate the world position of the tile based on its center
    val worldX = gridX * tileSize + tileSize / 2
    val worldZ = gridZ * tileSize + tileSize / 2

    // ... Additional methods and properties
}

