package com.fluxtah.application.api.ai

import com.fluxtah.application.api.entity.Entity
import com.fluxtah.application.api.scene.Scene

abstract class AiSchedule {
    lateinit var scene: Scene
    lateinit var entity: Entity
    lateinit var machine: AiEntityStateMachine<*>
    open fun enter() = Unit
    abstract fun update(time: Float, deltaTime: Float)
    open fun exit() = Unit
}