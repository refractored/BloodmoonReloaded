package net.refractored.bloodmoonreloaded.types.activation

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.activation.implementation.ActivationMethod
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld

/**
 * Represents a world that will start after another bloodmoon starts.
 * The "Mirrored" bloodmoon will not have the same settings.
 */
class MirrorActivation(
    bloodmoonWorld: BloodmoonWorld,
) : ActivationMethod(bloodmoonWorld) {

    override fun getInfo(): ComponentLike = BloodmoonPlugin.Companion.instance.langYml
        .getStringPrefixed("messages.info.success.mirror")
        .replace("%world%", bloodmoonWorld.world.name)
        .replace("%mirror_world%", bloodmoonWorld.world.name)
        .replace("%status%", this.bloodmoonWorld.status.miniMessage())
        .miniToComponent()

    val mirrorWorld = BloodmoonRegistry.getWorld(bloodmoonWorld.config.getString("mirror.world")) ?: throw IllegalArgumentException("Config does not contain a valid bloodmoon world.")

    override fun shouldActivate(): Boolean {
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            return false
        }
        return mirrorWorld.status == BloodmoonWorld.Status.ACTIVE
    }

}
