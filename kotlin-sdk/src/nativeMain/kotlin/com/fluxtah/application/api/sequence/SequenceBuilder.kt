package com.fluxtah.application.api.sequence

import com.fluxtah.application.api.scene.Scene
import com.fluxtah.application.api.scene.SceneComponent

class SequenceBuilder {
    private val actions = mutableListOf<() -> Action>()

    fun build(): Sequence {
        return Sequence(actions)
    }

    /**
     * Wait for a duration of time before continuing to the next step in the sequence.
     */
    fun wait(
        duration: Float,
        onWait: OnWaitContext.() -> Unit = { }
    ) {
        actions.add { Action.Wait(duration, onWait) }
    }

    fun action(block: (Scene, Any) -> Unit) {
        actions.add { Action.InstantAction(block) }
    }

    fun setCamera(cameraId: String) {
        actions.add { Action.SetCameraAction(cameraId) }
    }

    fun <T : SceneComponent> onComponent(componentId: String, block: (Scene, T, Any) -> Unit) {
        actions.add {
            Action.InstantAction { scene, data ->
                block.invoke(
                    scene,
                    scene.componentById(componentId),
                    data
                )
            }
        }
    }

    fun activateComponent(componentId: String) {
        actions.add {
            Action.InstantAction { scene, _ ->
                scene.componentById<SceneComponent>(componentId).activate()
            }
        }
    }

    fun deactivateComponent(componentId: String) {
        actions.add {
            Action.InstantAction { scene, _ ->
                scene.componentById<SceneComponent>(componentId).deactivate()
            }
        }
    }

    fun <T> onSceneData(block: T.(Scene, Any) -> Unit) {
        actions.add {
            Action.InstantAction { scene, data ->
                block.invoke(
                    scene.data(),
                    scene,
                    data
                )
            }
        }
    }

    fun resetEntityPool(entityPoolId: String) {
        actions.add {
            Action.InstantAction { scene, _ ->
                scene.resetEntityPool(entityPoolId)
            }
        }
    }

    fun startSequence(sequenceId: String) {
        actions.add {
            Action.InstantAction { scene, _ ->
                scene.createSequence(sequenceId)!!.play()
            }
        }
    }
}

class OnWaitContext(
    private val _finish: () -> Unit
) {
    lateinit var scene: Scene
    var time: Float = 0.0f
    var deltaTime: Float = 0.0f
    var timeRemaining: Float = 0.0f
    var sequenceData: Any = Any()
    fun finish() {
        _finish.invoke()
    }
}

abstract class Action {
    abstract fun play(scene: Scene, data: Any, time: Float, deltaTime: Float)
    abstract fun isComplete(): Boolean

    class Wait(
        private val duration: Float,
        private val onWait: OnWaitContext.() -> Unit
    ) : Action() {
        private var timeElapsed = 0.0f

        private val onWaitContext = OnWaitContext { timeElapsed = duration }

        override fun play(scene: Scene, data: Any, time: Float, deltaTime: Float) {
            timeElapsed += deltaTime
            onWaitContext.scene = scene
            onWaitContext.time = time
            onWaitContext.deltaTime = deltaTime
            onWaitContext.timeRemaining = duration - timeElapsed
            onWaitContext.sequenceData = data
            onWait.invoke(onWaitContext)
        }

        override fun isComplete(): Boolean {
            return timeElapsed >= duration
        }
    }

    class InstantAction(private val action: (Scene, Any) -> Unit) : Action() {
        override fun play(scene: Scene, data: Any, time: Float, deltaTime: Float) = action.invoke(scene, data)
        override fun isComplete(): Boolean = true
    }

    class SetCameraAction(private val cameraId: String) : Action() {
        override fun play(scene: Scene, data: Any, time: Float, deltaTime: Float) {
            scene.setActiveCamera(cameraId)
        }

        override fun isComplete(): Boolean = true
    }
}
