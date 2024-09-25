package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import org.bukkit.World

/**
 * Represents a world that will never start a bloodmoon on its own.
 */
class NoneBloodmoon(
    override var world: World,
    override var config: Config
) : BloodmoonWorld(world, config) {
    override fun shouldActivate(): Boolean = false
}
