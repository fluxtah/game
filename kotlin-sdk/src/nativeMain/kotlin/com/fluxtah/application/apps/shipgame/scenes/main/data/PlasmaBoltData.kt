package com.fluxtah.application.apps.shipgame.scenes.main.data

import com.fluxtah.application.api.entity.Entity

data class PlasmaBoltData(
    var team: Team = Team.None,
    var owner: Entity? = null
)