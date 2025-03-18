package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import org.bukkit.World

/**
 * Represents a world that will never start a bloodmoon on its own.
 */
class NoneBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    override fun getInfo():  ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.bloodmoon-info-none")
        .replace("%world%", world.name)
        .replace("%status%", this.status.miniMessage())
        .miniToComponent()

    val permanentBloodmoon: Boolean = config.getBool("NoneStatus")

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        return permanentBloodmoon
    }

    override fun onActivation() {
        if (!permanentBloodmoon) return

        expiryTime = -1
    }

    companion object : TypeRegistry.BloodmoonWorldFactory {
        override fun create(world: World, config: Config): BloodmoonWorld {
            return NoneBloodmoon(world, config)
        }
    }
}
