package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniPrefix
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

/**
 * Represents the configuration and state of a bloodmoon in a specific world.
 *
 * This abstract class provides the common functionality and properties required to manage
 * a bloodmoon event, including activation and deactivation logic, configuration settings,
 * and persistent data management.
 *
 * @property world The world in which the bloodmoon event occurs.
 * @property config The configuration settings for the bloodmoon event.
 */

abstract class BloodmoonWorld(
    var world: World,
    var config: Config
) : Holder,
    Registrable {



    /**
     * Represents the status of the bloodmoon event.
     */
    enum class Status {
        ACTIVE,
        INACTIVE,

        /**
         * The bloodmoon is currently activating.
         * This usually means the time is being transitioned to night.
         */
        ACTIVATING;

        /**
         * @return The component from the lang.yml
         */
        fun component() = BloodmoonPlugin.instance.langYml.getString("bloodmoon-status.${name.lowercase()}").miniToComponent()

        /**
         * @return The plaintext from the lang.yml
         */
        fun plaintext() = PlainTextComponentSerializer.plainText().serialize(this.component())
    }

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_status_plain"
            ) {
                status.plaintext()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_status"
            ) {
                LegacyComponentSerializer.legacySection().serialize(status.component())
            }
        )
    }

    final override val id: NamespacedKey = NamespacedKey("bloodmoonreloaded", world.name)

    var status = Status.INACTIVE

    abstract var info: ComponentLike

    override val effects =
        Effects.compile(
            config.getSubsections("effects"),
            ViolationContext(BloodmoonPlugin.instance, "World ${world.name}")
        )

    override val conditions =
        Conditions.compile(
            config.getSubsections("conditions"),
            ViolationContext(BloodmoonPlugin.instance, "World ${world.name}")
        )

    /**
     * Represents whether the daylight cycle needs to be reverted.
     *
     * This property is `false` whenever the daylight cycle was reverted back to its original state,
     * and `true` whenever it needs to be reverted back.
     */
    var revertDaylightCycle: Boolean
        get() {
            return world.persistentDataContainer.get(BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_daylightcycle"),
                PersistentDataType.BOOLEAN
            ) ?: false
        }
        set(value) {
            world.persistentDataContainer.set(
                BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_daylightcycle"),
                PersistentDataType.BOOLEAN,
                value
            )
        }

    private val bloodmoonRemainingMillis =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_bloodmoon_remaining_millis"),
            PersistentDataKeyType.DOUBLE,
            0.0
        )

    /**
     * The remaining time in milliseconds of the bloodmoon.
     */
    var savedBloodmoonRemainingMillis: Double
        get() = Bukkit.getServer().profile.read(bloodmoonRemainingMillis)
        set(value) = Bukkit.getServer().profile.write(bloodmoonRemainingMillis, value)

    var fullTime: Long = 0L

    /**
     * The length in milliseconds of the bloodmoon set in the config.
     */
    val length: Long = config.getString("Length").toLong() * 1000

    val isIncreasing = config.getBool("Increasing")

    val bedDisabled = config.getBool("BedDisabled")

    val bossbarEnabled: Boolean = config.getBool("Bossbar.Enabled")

    val createFog: Boolean = config.getBool("Bossbar.Fog")

    val darkenScreen: Boolean = config.getBool("Bossbar.DarkenScreen")

    val setDaylightCycle: Boolean = config.getBool("SetDaylightCycle")

    val setThunder: Boolean = config.getBool("SetThunder")

    val victoryChime: Boolean = config.getBool("VictoryChime")

    val periodicCaveSounds: Boolean = config.getBool("PeriodicCaveSounds.Enabled")

    val clearInventory: Boolean = config.getBool("PlayerDeath.ClearInventory")

    val clearEXP: Boolean = config.getBool("PlayerDeath.ClearEXP")

    val useCustomDeathMessage: Boolean = config.getBool("PlayerDeath.CustomDeathMessage")

    val disableChangeTime: Boolean = config.getBool("DisableChangeTime")

    val bossbarColor =
        BossBar.Color.entries.find { it.name == config.getString("Bossbar.Color") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Color")}")

    val bossbarStyle =
        BossBar.Overlay.entries.find { it.name == config.getString("Bossbar.Style") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Style")}")

    val customDeathMessage = config.getString("Messages.DeathMessage")

    val usePrefix = config.getBool("Messages.UsePrefix")

    val activationMessage = if (usePrefix) {
        BloodmoonPlugin.instance.langYml.miniPrefix() + config.getString("Messages.Activation")
    } else {
        config.getString("Messages.Activation")
    }

    val deactivationMessage = if (usePrefix) {
        BloodmoonPlugin.instance.langYml.miniPrefix() + config.getString("Messages.Deactivation")
    } else {
        config.getString("Messages.Deactivation")
    }

    val bedDenyMessage = if (usePrefix) {
        BloodmoonPlugin.instance.langYml.miniPrefix() + config.getString("Messages.BedDenyMessage")
    } else {
        config.getString("Messages.BedDenyMessage")
    }

    val activationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    val deactivationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    override fun getID(): String = world.name

    /**
     * Whether the bloodmoon is currently activating.
     */
    var bossbar =
        BossBar.bossBar(
            config
                .getString("Bossbar.Title")
                .miniToComponent(),
            1.0f,
            bossbarColor,
            bossbarStyle
        )

    /**
     * The time the bloodmoon expires.
     */
    var expiryTime = 0L

    /**
     * @return The remaining bloodmoon time in milliseconds
     */
    val remainingTime: Long
        get() {
            val time = System.currentTimeMillis()
            if ((expiryTime - time) <= 0) {
                return 0
            }
            return expiryTime - time
        }

    open fun onActivation() {}

    open fun onDeactivation() {}

    /**
     * Run periodic tasks.
     * This is run every 20 ticks.
     */
    open fun periodicTasks() {}

    /**
     * Run periodic tasks.
     * This is run every 20 ticks.
     */
    fun runPeriodicTasks() {
        if (status == Status.ACTIVE) {
            if (periodicCaveSounds && (Random.nextDouble(1.0) < config.getDouble("PeriodicCaveSounds.Chance"))) {
                for (player in world.players) {
                    player.playSound(player.location, "ambient.cave", 1.0f, 1.0f)
                }
            }

            savedBloodmoonRemainingMillis = (expiryTime - System.currentTimeMillis()).toDouble()

            if (setThunder) {
                world.setStorm(true)
            }
            world.fullTime = fullTime
        }

        // In case, a bloodmoon wants to do some special stuff im not sure.
        this.periodicTasks()
    }

    /**
     * This function should check all conditions and return whether the bloodmoon should activate.
     *
     * This function is ran every two ticks.
     */
    abstract fun shouldActivate(): Boolean

    fun activate(
        /**
         * The length in milliseconds of the bloodmoon.
         */
        length: Long = this.length,
        /**
         * Whether to announce the activation.
         * This is true by default.
         * The message won't be sent if the config disables the message.
         */
        announce: Boolean = true
    ) {
        if (status != Status.INACTIVE) {
            throw IllegalStateException("Bloodmoon is already active.")
        }
        val event = BloodmoonStartEvent(world, this)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        status = Status.ACTIVATING
        activationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce && config.getBool("Messages.DeactivationEnabled")) {
            if (config.getBool("Messages.ActivationAnnounceGlobal")) {
                Bukkit.broadcast(activationMessage.miniToComponent())
            } else {
                world.players.forEach { player ->
                    player.sendMessage(activationMessage.miniToComponent())
                }
            }
        }
        if (disableChangeTime) {
            activateBloodmoon(length)
            status = Status.ACTIVE
            onActivation()
            return
        }
        val nightTimeTransition =
            object : BukkitRunnable() {
                override fun run() {
                    if (world.time in 17500..18500) {
                        cancel()
                        activateBloodmoon(length)
                        status = Status.ACTIVE
                        onActivation()
                        return
                    }

                    val timeDifference = world.time - 18000

                    world.time += if (timeDifference < 0) 70 else -70
                }
            }
        nightTimeTransition.runTaskTimer(BloodmoonPlugin.instance, 0, 1)
    }

    /**
     * Enables the bloodmoon.
     */
    private fun activateBloodmoon(length: Long) {
        if (setDaylightCycle) {
            revertDaylightCycle = true
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }

        expiryTime = System.currentTimeMillis() + length
        fullTime = world.fullTime
        savedBloodmoonRemainingMillis = length.toDouble()

        if (!bossbarEnabled) return

        world.players.forEach {
            bossbar.addViewer(it)
        }
        if (createFog) {
            bossbar.addFlags(BossBar.Flag.CREATE_WORLD_FOG)
        }
        if (darkenScreen) {
            bossbar.addFlags(BossBar.Flag.DARKEN_SCREEN)
        }

        // Runnable to update bossbar.
        object : BukkitRunnable() {
            override fun run() {
                val progress =
                    if (!isIncreasing) {
                        val elapsedTime = System.currentTimeMillis() - (expiryTime - length)
                        (elapsedTime.toDouble() / length.toDouble()).coerceIn(0.0, 1.0).toFloat()
                    } else {
                        val remainingTime = expiryTime - System.currentTimeMillis()
                        (remainingTime.toDouble() / length.toDouble()).coerceIn(0.0, 1.0).toFloat()
                    }
                bossbar.progress(progress)
                if (status == Status.INACTIVE) {
                    cancel()
                    return
                }
            }
        }.runTaskTimer(BloodmoonPlugin.instance, 1, 1)
    }

    private fun revertSettings() {
        if (revertDaylightCycle) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        }
        if (setThunder) {
            world.clearWeatherDuration = 20 * 60 * 20
            world.setStorm(false)
        }
        world.players.forEach { player ->
            bossbar.removeViewer(player)
        }
    }

    /**
     * Deactivate the bloodmoon.
     */
    fun deactivate(
        reason: StopCause = StopCause.PLUGIN,
        announce: Boolean = true
    ) {
        if (status == Status.INACTIVE) throw IllegalStateException("Bloodmoon is not active.")
        val event = BloodmoonStopEvent(world, this, reason)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        // Run, if event is not cancelled
        // This comment is here so I remember to not be stupid and add stuff before the event is fired.

        deactivationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce && config.getBool("Messages.DeactivationEnabled")) {
            if (config.getBool("Messages.DeactivationAnnounceGlobal")) {
                Bukkit.broadcast(deactivationMessage.miniToComponent())
            } else {
                world.players.forEach { player ->
                    player.sendMessage(deactivationMessage.miniToComponent())
                }
            }
        }
        revertSettings()
        if (reason != StopCause.TIMER) return
        savedBloodmoonRemainingMillis = 0.0
        status = Status.INACTIVE
        if (victoryChime) {
            for (player in world.players) {
                player.playSound(player.location, "ui.toast.challenge_complete", 1.0f, 1.0f)
            }
        }
        this.onDeactivation()
    }

    override fun onRemove() {
        if (status == Status.ACTIVE) {
            deactivate(StopCause.UNLOAD, false)
        }
    }
}
