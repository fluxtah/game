package com.fluxtah.application.apps.shipgame.scenes.main.data

data class GameData(
    var sceneState: GameSceneState = GameSceneState.Lobby,
    var lobbyTimeRemaining: Float = 0f,
    var gameTimeRemaining: Float = 0f,
    var aiShipsEnabled: Boolean = false,
    val aiShipsCombatRange: Float = 100f,
    val friendlyTeam: TeamData = TeamData(Team.Friendly, skin = 1),
    val enemyTeam: TeamData = TeamData(Team.Enemy, skin = 2),
    var localPlayerData: PlayerData? = null,

    val shipNoEnergyFirePenaltyFactor: Float = 4f,
    val shipNoEnergyMovePenaltyFactor: Float = 0.5f,

    val powerNodeCost: Float = 70f,
    val placePowerNodeCoolDown: Float = 10f,

    val powerNodeKillEnergyBonus: Float = 20f,
    val enemyShipKillEnergyBonus: Float = 10f
) {
    val plasmaBoltDamage: Float = 14f
}

data class TeamData(
    val team: Team,
    val skin: Int = 0,
    val players: MutableList<PlayerData> = mutableListOf(),
    var score: Float = 0f,
    var placePowerNodeCoolDownTimer: Float = 0f
)

