package net.refractored.bloodmoonreloaded.worlds

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

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

    override val id: NamespacedKey = NamespacedKey("bloodmoonreloaded", world.name)

    private val daysUntilKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_days_until"),
            PersistentDataKeyType.INT,
            0,
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
    var savedDaysUntilActivation: Int
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

    var daysUntilActivation: Int = savedDaysUntilActivation

    /**
     * Represents whether the daylight cycle needs to be reverted.
     *
     * This property is `false` whenever the daylight cycle was reverted back to its original state,
     * and `true` whenever it needs to be reverted back.
     */
    var revertDaylightCycle: Boolean
        get() {
            return world.persistentDataContainer.get(
                NamespacedKey(BloodmoonPlugin.instance, "daylightcycle"),
                PersistentDataType.BOOLEAN,
            ) ?: false
        }
        set(value) {
            world.persistentDataContainer.set(
                NamespacedKey(BloodmoonPlugin.instance, "daylightcycle"),
                PersistentDataType.BOOLEAN,
                value,
            )
        }

    /**
     * The length in milliseconds of the bloodmoon.
     */
    val length: Long = config.getString("Length").toLong() * 1000

    val isIncreasing = config.getBool("Increasing")
    val bossbarEnabled: Boolean = config.getBool("Bossbar.Enabled")
    val createFog: Boolean = config.getBool("Bossbar.Fog")
    val darkenScreen: Boolean = config.getBool("Bossbar.DarkenScreen")

    val setDaylightCycle: Boolean = config.getBool("SetDaylightCycle")

    val bossbarColor =
        BossBar.Color.entries.find { it.name == config.getString("Bossbar.Color") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Color")}")

    val bossbarStyle =
        BossBar.Overlay.entries.find { it.name == config.getString("Bossbar.Style") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Style")}")

    val activationType = BloodmoonActivation.valueOf(config.getString("BloodmoonActivate").uppercase())

    enum class BloodmoonActivation {
        DAYS,
        TIMED,
        NONE,
    }

    /**
     * The last value of [World.isDayTime] in the last tick.
     */
    var lastDaytimeCheck: Boolean = false

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

        if (revertDaylightCycle) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        }

        if (savedBloodmoonRemainingMillis > 0) {
            active = ActiveBloodmoon(this, savedBloodmoonRemainingMillis.toLong())
        }
    }

    /**
     * Whether the bloodmoon is currently activating.
     */
    var activating: Boolean = false

    fun activate(
        /**
         * The length in milliseconds of the bloodmoon.
         */
        length: Long = this.length,
        announce: Boolean = true,
    ) {
        if (active != null) {
            throw IllegalStateException("Bloodmoon is already active.")
        }
        val event = BloodmoonStartEvent(world, this)
        event.callEvent()
        activating = true
        if (event.isCancelled()) {
            return
        }

        activationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce) {
            world.players.forEach { player ->
                player.sendMessage(activationMessage.miniToComponent())
            }
        }
        val nightTimeTransition =
            object : BukkitRunnable() {
                override fun run() {
                    if (world.time in 17500..18500) {
                        cancel()
                        active = ActiveBloodmoon(this@BloodmoonWorld, length)
                        activating = false
                        return
                    }

                    val timeDifference = world.time - 18000

                    world.time += if (timeDifference < 0) 70 else -70
                }
            }
        nightTimeTransition.runTaskTimer(BloodmoonPlugin.instance, 0, 1)
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
        if (setDaylightCycle) {
            revertDaylightCycle = true
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }

        deactivationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce) {
            world.players.forEach { player ->
                player.sendMessage(deactivationMessage.miniToComponent())
            }
        }

        savedBloodmoonRemainingMillis = 0.0
        daysUntilActivation = 0
        world.setStorm(false)
        world.clearWeatherDuration = 20 * 60 * 20
        world.players.forEach { player ->
            active?.bossbar?.removeViewer(player)
        }
        active = null
    }

    override fun onRemove() {
        active?.let { deactivate(StopCause.UNLOAD) }
    }
}
