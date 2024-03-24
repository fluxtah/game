package com.fluxtah.application.apps.shipgame

import com.fluxtah.application.api.Application
import com.fluxtah.application.api.scene.setActiveScene
import com.fluxtah.application.apps.shipgame.scenes.mainScene
import com.fluxtah.application.apps.shipgame.scenes.testScene

/**
 *  TODO:
 *     - Special maneuvers (cost energy)
 *       - Barrel roll - invulnerable for a short time - fast acceleration boost forwards whilst spinning
 *       - Boost - fast acceleration forwards
*        - Loop - fast acceleration upwards and then back on yourself/enemy, good for getting behind enemy
            when they're chasing you
 *    - Name tags for ships
 *       - Add a mini map
 *    - Add map bounds (make map rectangular where teams start on opposite sides)
 *    - orient particles to face any direction including bill-boarding
 *    - Rockets and flares!
 *       - Target enemies and cycle through targets
 *       - Information about targets (health, name, etc)
 *    - Bouncing bombs!!
 *       - As an alternative to rockets
 *       - Bounce off walls
 *    - Turret tiles, place a turret on a tile to defend it whilst you're away
 *       - turret tiles require nearby power node to function
 *    - Wall tiles, place a wall to block enemy ships
 *       - Walls you can shoot through but not fly through
 *       - Walls you can fly through but not shoot through
 *    - Scene switching
 *       - Main menu
 *       - In Game
 *    - Improve logging
 *    - Visual indicators for power node area of effect
 *    - Visual indicator for ship shield taking hits
 *    - Ship fire and smoke as it goes into a critical state (<50% health, for example)
 **/
class ShipGame : Application {

    companion object {
        const val PLAYERS_PER_TEAM = 1
        const val TEAM_WIN_POWER_LEVEL = 1000
    }

    override fun initialize() {
        testScene()
        mainScene()

      //   setActiveScene("test")
        setActiveScene(Id.SCENE_MAIN)
    }
}

