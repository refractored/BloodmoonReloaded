package net.refractored.bloodmoonreloaded.bloodmoon.activation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.bloodmoon.activation.implementation.ActivationMethod
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld

/**
 * Represents a world that will never start a bloodmoon on its own.
 */
class NoActivation(
    bloodmoonWorld: BloodmoonWorld,
) : ActivationMethod(bloodmoonWorld) {

    override fun getInfo(): ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.info.success.none")
        .replace("%world%", bloodmoonWorld.world.name)
        .replace("%status%", this.bloodmoonWorld.status.miniMessage())
        .miniToComponent()

    val permanentBloodmoon: Boolean = bloodmoonWorld.config.getBool("none.always-active")

    override fun checkStatus(): Boolean {
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            return false
        }
        return permanentBloodmoon
    }

    override fun onActivation() {
        if (!permanentBloodmoon) return

        bloodmoonWorld.expiryTime = -1
    }

}
