package com.fluxtah.application.apps.shipgame.behaviors.aiplayer

enum class AiPlayerShipState {
    Idle,
    MoveToEnemy,
    AttackEnemy,
    PlacePowerNode,
    Defending,
    AttackPowerNode,
    SeekPowerNode,
    FleeEnemy,
}