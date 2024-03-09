package com.fluxtah.application.api.sequence

import com.fluxtah.application.api.scene.BaseScene
import com.fluxtah.application.api.scene.Scene

class Sequence(private val actions: MutableList<() -> Action>) {
    lateinit var scene: Scene
    var data: Any = Any()

    private var actionIndex = 0
    private var currentAction: Action? = null

    fun play(data: Any = Any()) {
        this.data = data
        (scene as BaseScene).dispatchSequence(this)
    }

    fun advance(time: Float, deltaTime: Float) {
        if (currentAction == null) {
            currentAction = actions[actionIndex].invoke()
        }

        currentAction?.play(scene, data, time, deltaTime)

        if (currentAction?.isComplete() == true) {
            actionIndex++
            currentAction = actions.getOrNull(actionIndex)?.invoke()

            if (currentAction == null) {
                (scene as BaseScene).stopSequence(this)
            }
        }
    }

    fun reset() {
        (scene as BaseScene).stopSequence(this)
        actionIndex = 0
        currentAction = null
    }
}