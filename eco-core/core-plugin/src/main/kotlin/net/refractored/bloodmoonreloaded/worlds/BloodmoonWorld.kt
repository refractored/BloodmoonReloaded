package net.refractored.bloodmoonreloaded.worlds

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.toComponent
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
import org.bukkit.Bukkit
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

    override val id: NamespacedKey = NamespacedKey("bloodmoonReloaded", world.name)

    private val daysUntilKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_days_until"),
            PersistentDataKeyType.DOUBLE,
            0.0,
        )

    private val millisUntilKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_millis_until"),
            PersistentDataKeyType.DOUBLE,
            0.0,
        )

    private val bloodmoonRemainingMillis =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_bloodmoon_remaining_millis"),
            PersistentDataKeyType.DOUBLE,
            0.0,
        )

    var savedBloodmoonRemainingMillis: Double
        get() = Bukkit.getServer().profile.read(bloodmoonRemainingMillis)
        set(value) = Bukkit.getServer().profile.write(bloodmoonRemainingMillis, value)

    /**
     * The time in days that the bloodmoon will expire.
     * Before using make sure to check if the [activationType] is [BloodmoonActivation.DAYS].
     * @see activationType
     */
    var savedDaysUntilActivation: Double
        get() = Bukkit.getServer().profile.read(daysUntilKey)
        set(value) = Bukkit.getServer().profile.write(daysUntilKey, value)

    /**
     * The time in Milliseconds that the bloodmoon will expire.
     * Before using make sure to check if the [activationType] is [BloodmoonActivation.TIMED].
     * @see activationType
     */
    var savedMillisUntilActivation: Double
        get() = Bukkit.getServer().profile.read(millisUntilKey)
        set(value) = Bukkit.getServer().profile.write(millisUntilKey, value)

    var millisUntilActivation: Double = savedMillisUntilActivation

    val daysUntilActivation: Double = savedDaysUntilActivation

    /**
     * The length in milliseconds of the bloodmoon.
     */
    val length: Long = config.getString("Length").toLong() * 1000

    val bossbarEnabled: Boolean = config.getBool("Bossbar.Enabled")

    val bossbarColor = BossBar.Color.valueOf(config.getString("Bossbar.Color"))

    val bossbarStyle = BarStyle.valueOf(config.getString("Bossbar.Style"))

    val activationType = BloodmoonActivation.valueOf(config.getString("BloodmoonActivate").uppercase())

    enum class BloodmoonActivation {
        DAYS,
        TIMED,
        NONE,
    }

    val activationDays = config.getInt("Days").toLong()

    val activationTime = config.getString("Timed").toLong()

    val usePrefix = config.getBool("UsePrefix")

    val activationMessage = config.getString("Messages.Activation")

    val deactivationMessage = config.getString("Messages.Deactivation")

    val activationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    val deactivationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    override fun getID(): String = world.name

    var active: ActiveBloodmoon? = null
        private set

    init {

        if (savedBloodmoonRemainingMillis > 0) {
            active = ActiveBloodmoon(this, savedBloodmoonRemainingMillis.toLong())
        }
    }

    fun activate(
        length: Long = this.length,
        announce: Boolean = true,
    ) {
        if (active != null) {
            throw IllegalStateException("Bloodmoon is already active.")
        }
        val event = BloodmoonStartEvent(world, this)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        activationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce) {
            world.players.forEach { player ->
                player.sendMessage(activationMessage.toComponent())
            }
        }
        active = ActiveBloodmoon(this, length)
    }

    /**
     * Deactivate the bloodmoon.
     */
    fun deactivate(
        reason: StopCause = StopCause.PLUGIN,
        announce: Boolean = true,
    ) {
        active ?: throw IllegalStateException("Bloodmoon is not active.")
        val event = BloodmoonStopEvent(world, this, reason)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        deactivationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce) {
            world.players.forEach { player ->
                player.sendMessage(deactivationMessage.toComponent())
            }
        }
        savedBloodmoonRemainingMillis = 0.0
        active = null
    }

    override fun onRemove() {
        active?.let { deactivate(StopCause.UNLOAD) }
    }
}
