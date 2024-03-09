package com.fluxtah.application.apps.shipgame.scenes.main.data

data class PowerNodeData(
    var team: Team = Team.None,
    var health: Float = 100.0f,
    val rechargeRate: Float = 0.5f,
    val rechargeDistance: Float = 140.0f,
    var lastRechargeTime: Float = 0.0f,
    var replenishAmount: Float = 1.2f
) {
    fun depleteHealth(amount: Float): Float {
        health -= amount
        if (health < 0) {
            val remainder = health * -1
            health = 0f
            return remainder
        }

        return 0f
    }

    fun resetAll(){
        health = 100.0f
        lastRechargeTime = 0.0f
    }
}