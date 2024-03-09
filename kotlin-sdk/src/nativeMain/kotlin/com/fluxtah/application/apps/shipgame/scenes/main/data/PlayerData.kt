package com.fluxtah.application.apps.shipgame.scenes.main.data

import com.fluxtah.application.api.math.Vector3

data class PlayerData(
    val isBot: Boolean = false,
    val id: Int = -1,
    val team: Team = Team.None,
    var name: String = "",
    var kills: Int = 0,
    var deaths: Int = 0,
    var startPos: Vector3 = Vector3(),
) {
    fun isLocalPlayer() = id == 0
}