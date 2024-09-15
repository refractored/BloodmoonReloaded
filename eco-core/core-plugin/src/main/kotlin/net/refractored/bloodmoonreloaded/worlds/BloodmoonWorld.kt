package net.refractored.bloodmoonreloaded.worlds

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.NamespacedKey
import org.bukkit.World

/**
 * Represents a world and its settings for bloodmoon.
 */
class BloodmoonWorld(
    var world: World,
    var config: Config,
) : Holder,
    Registrable {
    override val effects =
        Effects.compile(
            config.getSubsections("effects"),
            ViolationContext(BloodmoonPlugin.instance, "World ${world.name}"),
        )

    override val conditions =
        Conditions.compile(
            config.getSubsections("conditions"),
            ViolationContext(BloodmoonPlugin.instance, "World ${world.name}"),
        )
    override val id: NamespacedKey = NamespacedKey(world.name, "bloodmoon")

    override fun getID(): String = world.name

    var active: ActiveBloodmoon? = null
        private set

    /**
     * Deactivate the bloodmoon.
     */
    fun deactivate() {
        active ?: throw IllegalStateException("Bloodmoon is not active.")
        active = null
    }

    override fun onRemove() {
        active?.let { deactivate() }
    }
}
