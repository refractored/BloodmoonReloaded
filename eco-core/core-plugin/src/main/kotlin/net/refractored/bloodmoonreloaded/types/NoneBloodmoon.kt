package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import org.bukkit.World

/**
 * Represents a world that will never start a bloodmoon on its own.
 */
class NoneBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    val permanentBloodmoon: Boolean
        get() = config.getBool("NoneStatus")

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
}
