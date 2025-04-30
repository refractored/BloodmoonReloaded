package net.refractored.bloodmoonreloaded.types.deactivation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld

abstract class DeactivationMethod(val bloodmoonWorld: BloodmoonWorld) {

    abstract fun shouldDeactivate(): Boolean

    abstract fun getInfo(): ComponentLike
}
