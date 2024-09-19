package net.refractored.bloodmoonreloaded.worlds

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.events.StopCause
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarStyle

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

    /**
     * The length in milliseconds of the bloodmoon.
     */
    val length: Long = config.getString("length").toLong() * 1000

    val bossbarEnabled: Boolean = config.getBool("Bossbar.Enabled")

    val bossbarColor = BossBar.Color.valueOf(config.getString("Bossbar.Color"))

    val bossbarStyle = BarStyle.valueOf(config.getString("Bossbar.Style"))

    val bloodmoonActivated = BloodmoonActivation.valueOf(config.getString("activation").uppercase())

    enum class BloodmoonActivation {
        DAYS,
        TIMED,
        NONE,
    }

    val activationDays = config.getInt("Days").toLong()

    val activationTime = config.getString("Time").toLong()

    val usePrefix = config.getBool("UsePrefix")

    val activationMessage = config.getString("Messages.Activation")

    val deactivationMessage = config.getString("Messages.Deactivation")

    val activationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    val deactivationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    override fun getID(): String = world.name

    var active: ActiveBloodmoon? = null
        private set

    fun activate(length: Long) {
        val event = BloodmoonStartEvent(world, this)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        active ?: throw IllegalStateException("Bloodmoon is already active.")
        active = ActiveBloodmoon(this)
    }

    /**
     * Deactivate the bloodmoon.
     */
    fun deactivate(reason: StopCause = StopCause.PLUGIN) {
        active ?: throw IllegalStateException("Bloodmoon is not active.")
        val event = BloodmoonStopEvent(world, this, reason)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        active = null
    }

    override fun onRemove() {
        active?.let { deactivate(StopCause.UNLOAD) }
    }
}
