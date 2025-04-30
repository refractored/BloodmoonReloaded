package net.refractored.bloodmoonreloaded.bloodmoon.activation.implementation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
import net.refractored.bloodmoonreloaded.bloodmoon.LifecycleMethod

abstract class ActivationMethod(override val bloodmoonWorld: BloodmoonWorld): LifecycleMethod {
}
