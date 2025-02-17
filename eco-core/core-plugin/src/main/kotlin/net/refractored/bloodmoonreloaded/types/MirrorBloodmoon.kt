package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.World

/**
 * Represents a world that will start after another bloodmoon starts.
 * The "Mirrored" bloodmoon will not have the same settings.
 */
class MirrorBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    override var info: ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.bloodmoon-info-mirror")
        .replace("%world%", world.name)
        .replace("%mirror_world%", world.name)
        .replace("%status%", this.status.miniMessage())
        .miniToComponent()

    val mirrorWorld = config.getString("MirrorWorld")

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        return BloodmoonRegistry.getWorld(mirrorWorld)?.status == Status.ACTIVE
    }
}
