package com.fluxtah.application.apps.shipgame.scenes.main.data

import com.fluxtah.application.api.math.Vector3

data class ShipData(
    var playerData: PlayerData = ANY_PLAYER,
    var shield: Float = MAX_SHIELD,
    var armor: Float = MAX_ARMOR,
    var energy: Float = MAX_ENERGY,
    val velocity: Vector3 = Vector3(0f, 0f, 0f),
    val plasmaCannonEnergyCost: Float = 1.0f,
    val plasmaCannonFireDelay: Float = 0.3f,
    val input: ShipInput = ShipInput(),
) {
    /**
     * Depletes shield first, then armor if shield is depleted
     *
     * @return the amount of damage that was not absorbed by shield or armor
     */
    fun depleteShieldThenArmor(amount: Float): Float {
        val shieldDamage = depleteShield(amount)
        if (shieldDamage > 0) {
            val armorDamage = depleteArmor(shieldDamage)
            if (armorDamage > 0) {
                return armorDamage
            }
        }
        return 0f
    }

    fun depleteShield(amount: Float): Float {
        shield -= amount
        if (shield < 0) {
            val remainder = shield * -1
            shield = 0f
            return remainder
        }

        return 0f
    }

    fun replenishShield(amount: Float) {
        shield += amount
        if (shield > MAX_SHIELD) {
            shield = MAX_SHIELD
        }
    }

    fun depleteEnergy(amount: Float): Float {
        energy -= amount
        if (energy < 0) {
            val remainder = energy * -1
            energy = 0f
            return remainder
        }

        return 0f
    }

    fun replenishEnergy(amount: Float) {
        energy += amount
        if (energy > MAX_ENERGY) {
            energy = MAX_ENERGY
        }
    }

    fun depleteArmor(amount: Float): Float {
        armor -= amount
        if (armor < 0) {
            val remainder = armor * -1
            armor = 0f
            return remainder
        }
        return 0f
    }

    private fun replenishArmor(amount: Float) {
        armor += amount
        if (armor > MAX_ARMOR) {
            armor = MAX_ARMOR
        }
    }

    fun resetShield() {
        shield = MAX_SHIELD
    }

    fun resetArmor() {
        armor = MAX_ARMOR
    }

    fun resetEnergy() {
        energy = MAX_ENERGY
    }

    fun resetAll() {
        resetShield()
        resetArmor()
        resetEnergy()
    }

    companion object {
        const val MAX_SHIELD = 100f
        const val MAX_ARMOR = 100f
        const val MAX_ENERGY = 100f
    }
}