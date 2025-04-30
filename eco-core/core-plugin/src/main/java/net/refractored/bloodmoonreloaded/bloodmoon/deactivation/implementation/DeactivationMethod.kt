package net.refractored.bloodmoonreloaded.bloodmoon.deactivation.implementation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
import net.refractored.bloodmoonreloaded.bloodmoon.LifecycleMethod
import org.bukkit.entity.Player

abstract class DeactivationMethod(override val bloodmoonWorld: BloodmoonWorld): LifecycleMethod {

    /**
     * If the DeactivationMethod has a custom thing to show (e.g. a bossbar) you can add them here.
     *
     * This is not called on activation. Use [LifecycleMethod.onActivation] for that.
     */
    open fun onPlayerJoin(player: Player) {}

    /**
     * If the DeactivationMethod has a custom thing to show (e.g. a bossbar) you can remove them here.
     *
     * This is not called on deactivation. Use [LifecycleMethod.onDeactivation] for that.
     */
    open fun onPlayerLeave(player: Player) {}
}
