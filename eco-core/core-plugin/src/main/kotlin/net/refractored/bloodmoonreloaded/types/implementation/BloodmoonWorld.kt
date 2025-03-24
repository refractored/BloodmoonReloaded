package net.refractored.bloodmoonreloaded.types.implementation

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
import com.willfp.libreforge.getStrings
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
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
         * This means the time is being transitioned to night.
         */
        ACTIVATING;

        fun miniMessage() = BloodmoonPlugin.instance.langYml.getString("bloodmoon-status.${name.lowercase()}")

        /**
         * @return The minimessage converted to a component.
         */
        fun component() = miniMessage().miniToComponent()

        /**
         * @return The component converted to plaintext.
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

    abstract fun getInfo(): ComponentLike

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
            return world.persistentDataContainer.get(
                BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_daylightcycle"),
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
    var savedBloodmoonRemainingMillis: Long
        get() = Bukkit.getServer().profile.read(bloodmoonRemainingMillis).toLong()
        set(value) = Bukkit.getServer().profile.write(bloodmoonRemainingMillis, value.toDouble())

    var fullTime: Long = 0L

    /**
     * The length in milliseconds of the bloodmoon set in the config.
     */
    val configLength: Long = config.getString("length").toLong() * 1000

    val activationCommands: List<String>? = if (config.getBool("on-activation.run-commands.enabled")) {
        config.getStrings("on-activation.run-commands.commands").map { it.replace("%world%", world.name) }
    } else {
        null
    }

    val deactivationCommands: List<String>? = if (config.getBool("on-deactivation.run-commands.enabled")) {
        config.getStrings("on-deactivation.run-commands.commands").map { it.replace("%world%", world.name) }
    } else {
        null
    }

    override fun getID(): String = world.name

    /**
     * Whether the bloodmoon is currently activating.
     * Null if bossbar is not enabled.
     */
    var bossbar: BossBar?

    init {
        if (config.getBool("while-active.bossbar.enabled")) {
            bossbar = BossBar.bossBar(
                config
                    .getString("while-active.bossbar.title")
                    .miniToComponent(),
                1.0f,
                (
                    BossBar.Color.entries.find { it.name == config.getString("while-active.bossbar.color") }
                        ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("while-active.bossbar.color")}")
                    ),
                (
                    BossBar.Overlay.entries.find { it.name == config.getString("while-active.bossbar.style") }
                        ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("while-active.bossbar.style")}")
                    )
            ).apply {
                config.getStrings("while-active.bossbar.flags").forEach { flag ->
                    addFlags(BossBar.Flag.valueOf(flag.uppercase()))
                }
            }
        } else {
            bossbar = null
        }
    }

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

    fun saveBloodmoonTime() {
        savedBloodmoonRemainingMillis = (expiryTime - System.currentTimeMillis())
    }

    /**
     * Run periodic tasks.
     * This is run every 20 ticks.
     */
    fun runPeriodicTasks() {
        if (status == Status.ACTIVE) {
            if (config.getBool("periodic-sounds.enabled") && (Random.nextDouble(1.0) < config.getDouble("periodic-cave-sounds.chance"))) {
                for (player in world.players) {
                    player.playSound(player.location, config.getStrings("periodic-sounds.sounds").random(), 1.0f, 1.0f)
                }
            }

            saveBloodmoonTime()

            if (config.getBool("while-active.weather.enabled")) {
                world.setStorm(config.getBool("while-active.weather.rain"))
                world.isThundering = config.getBool("while-active.weather.thunder")
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
        length: Long = this.configLength,
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
        activationCommands?.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce && config.getBool("on-activation.announce.enabled")) {
            if (config.getBool("on-activation.announce.global")) {
                Bukkit.broadcast(config.getString("on-activation.announce.message").miniToComponent())
            } else {
                world.players.forEach { player ->
                    player.sendMessage(config.getString("on-activation.announce.message").miniToComponent())
                }
            }
        }
        if (config.getBool("while-active.disable-time-change")) {
            activateBloodmoon(length)
            status = Status.ACTIVE
            playActivationSounds()
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
                        playActivationSounds()
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
        if (config.getBool("while-active.stop-daylight-cycle")) {
            revertDaylightCycle = true
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }

        expiryTime = System.currentTimeMillis() + length
        fullTime = world.fullTime
        savedBloodmoonRemainingMillis = length

        bossbar?.let { bossbar ->
            world.players.forEach {
                bossbar.addViewer(it)
            }

            // Runnable to update bossbar.
            object : BukkitRunnable() {
                override fun run() {
                    val progress =
                        if (config.getBool("while-active.bossbar.increasing")) {
                            val elapsedTime = System.currentTimeMillis() - (expiryTime - configLength)
                            (elapsedTime.toDouble() / configLength.toDouble()).coerceIn(0.0, 1.0).toFloat()
                        } else {
                            val remainingTime = expiryTime - System.currentTimeMillis()
                            (remainingTime.toDouble() / configLength.toDouble()).coerceIn(0.0, 1.0).toFloat()
                        }
                    bossbar.progress(progress)
                    if (status == Status.INACTIVE) {
                        cancel()
                        return
                    }
                }
            }.runTaskTimer(BloodmoonPlugin.instance, 1, 1)
        }
    }

    private fun revertSettings() {
        if (revertDaylightCycle) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        }

        bossbar?.let { bossbar ->
            world.players.forEach { player ->
                bossbar.removeViewer(player)
            }
        }
    }

    /**
     * Deactivates the bloodmoon.
     * @throws IllegalStateException if the bloodmoon is not active.
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

        deactivationCommands?.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce && config.getBool("on-deactivation.announce.enabled")) {
            if (config.getBool("on-deactivation.announce.global")) {
                Bukkit.broadcast(config.getString("on-deactivation.announce.message").miniToComponent())
            } else {
                world.players.forEach { player ->
                    player.sendMessage(config.getString("on-deactivation.announce.message").miniToComponent())
                }
            }
        }
        status = Status.INACTIVE
        revertSettings()
        if (config.getBool("on-deactivation.weather.enabled")) {
            world.setStorm(config.getBool("on-deactivation.weather.rain"))
            world.isThundering = config.getBool("on-deactivation.weather.thunder")
        }
        if (reason == StopCause.RESTART || reason == StopCause.UNLOAD) return
        savedBloodmoonRemainingMillis = 0L
        playDeactivationSounds()
        this.onDeactivation()
    }

    fun playActivationSounds() {
        if (config.getBool("on-activation.sound.enabled")) {
            for (player in world.players) {
                player.playSound(player.location, config.getString("on-activation.sound.name"), config.getDouble("on-activation.sound.volume").toFloat(), config.getDouble("on-activation.sound.pitch").toFloat())
            }
        }
    }

    fun playDeactivationSounds() {
        if (config.getBool("on-deactivation.sound.enabled")) {
            for (player in world.players) {
                player.playSound(player.location, config.getString("on-deactivation.sound.name"), config.getDouble("on-deactivation.sound.volume").toFloat(), config.getDouble("on-deactivation.sound.pitch").toFloat())
            }
        }
    }

    override fun onRemove() {
        if (status == Status.ACTIVE) {
            deactivate(StopCause.UNLOAD, false)
        }
    }
}
