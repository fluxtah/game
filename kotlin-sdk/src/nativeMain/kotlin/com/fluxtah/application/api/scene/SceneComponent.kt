package com.fluxtah.application.api.scene

abstract class SceneComponent {
    lateinit var scene: Scene
    var onBuildScene: SceneBuilder.() -> Unit = {}
    open fun initialize() = Unit
    open fun onSceneCreated() = Unit
    open fun onSceneUpdate(time: Float) = Unit
    open fun onBeforeSceneUpdate(time: Float, deltaTime: Float) = Unit
    open fun onAfterSceneUpdate(time: Float, deltaTime: Float) = Unit
    open fun activate() = Unit
    open fun deactivate() = Unit
}