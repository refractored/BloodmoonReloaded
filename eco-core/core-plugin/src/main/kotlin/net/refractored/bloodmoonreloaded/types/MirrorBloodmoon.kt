package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import org.bukkit.World

/**
 * Represents a world that will start after another bloodmoon starts.
 * The "Mirrored" bloodmoon will not have the same settings.
 */
class  MirrorBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    override fun getInfo():  ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.bloodmoon-info-mirror")
        .replace("%world%", world.name)
        .replace("%mirror_world%", world.name)
        .replace("%status%", this.status.miniMessage())
        .miniToComponent()

        val mirrorWorld = BloodmoonRegistry.getWorld(config.getString("MirrorWorld")) ?: throw IllegalArgumentException("Config does not contain a valid bloodmoon world.")

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        return mirrorWorld.status == Status.ACTIVE
    }

    companion object : TypeRegistry.BloodmoonWorldFactory {
        override fun create(world: World, config: Config): BloodmoonWorld {
            return MirrorBloodmoon(world, config)
        }
    }
}
