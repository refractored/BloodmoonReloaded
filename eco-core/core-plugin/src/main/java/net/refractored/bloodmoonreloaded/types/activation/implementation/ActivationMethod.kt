package net.refractored.bloodmoonreloaded.types.activation.implementation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld

abstract class ActivationMethod(val bloodmoonWorld: BloodmoonWorld) {

    abstract fun shouldActivate(): Boolean

    abstract fun getInfo(): ComponentLike

    open fun onActivation() {}

    open fun onDeactivation() {}

    open fun periodicTasks() {}
}
