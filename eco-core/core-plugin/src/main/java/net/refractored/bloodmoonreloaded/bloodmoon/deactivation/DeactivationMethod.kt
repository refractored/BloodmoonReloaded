package net.refractored.bloodmoonreloaded.bloodmoon.deactivation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld

abstract class DeactivationMethod(val bloodmoonWorld: BloodmoonWorld) {

    abstract fun shouldDeactivate(): Boolean

    abstract fun getInfo(): ComponentLike
}
