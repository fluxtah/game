package com.fluxtah.application.apps.shipgame.logic

import com.fluxtah.application.apps.shipgame.ShipGame
import com.fluxtah.application.apps.shipgame.scenes.main.BOT_NAMES
import com.fluxtah.application.apps.shipgame.scenes.main.data.GameData
import com.fluxtah.application.apps.shipgame.scenes.main.data.PlayerData
import com.fluxtah.application.apps.shipgame.scenes.main.data.Team
import kotlin.random.Random

fun initPlayers(sceneData: GameData) {
    var friendlyTeamPlacesRemaining = ShipGame.PLAYERS_PER_TEAM - 1 // For the local player
    var enemyTeamPlacesRemaining = ShipGame.PLAYERS_PER_TEAM

    sceneData.friendlyTeam.players.clear()
    sceneData.enemyTeam.players.clear()

    val localPlayerData = PlayerData(
        id = 0,
        name = "Player",
        team = Team.Friendly,
        isBot = false
    )

    sceneData.localPlayerData = localPlayerData

    sceneData.friendlyTeam.players.add(localPlayerData)

    // Create player data, starting with bot players, as players
    // join they will replace the bot players
    for (i in 0 until (ShipGame.PLAYERS_PER_TEAM * 2) - 1) {
        val randomTeam = if (friendlyTeamPlacesRemaining > 0 && enemyTeamPlacesRemaining > 0) {
            if (Random.nextFloat() < 0.5) {
                friendlyTeamPlacesRemaining--
                Team.Friendly
            } else {
                enemyTeamPlacesRemaining--
                Team.Enemy
            }

        } else if (friendlyTeamPlacesRemaining > 0) {
            friendlyTeamPlacesRemaining--
            Team.Friendly
        } else {
            enemyTeamPlacesRemaining--
            Team.Enemy
        }

        val playerData = PlayerData(
            id = -1,
            name = BOT_NAMES[i],
            team = randomTeam,
            isBot = true
        )

        if (randomTeam == Team.Friendly) {
            sceneData.friendlyTeam.players.add(playerData)
        } else {
            sceneData.enemyTeam.players.add(playerData)
        }
    }
}
