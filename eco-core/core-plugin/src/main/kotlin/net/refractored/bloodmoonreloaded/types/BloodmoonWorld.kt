package net.refractored.bloodmoonreloaded.types

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
import net.refractored.bloodmoonreloaded.registry.ActiveBloodmoon
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

abstract class BloodmoonWorld(
    open var world: World,
    open var config: Config
) : Holder,
    Registrable {

    override val id: NamespacedKey = NamespacedKey("bloodmoonreloaded", world.name)

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
                NamespacedKey(BloodmoonPlugin.instance, "${id.key}_daylightcycle"),
                PersistentDataType.BOOLEAN
            ) ?: false
        }
        set(value) {
            world.persistentDataContainer.set(
                NamespacedKey(BloodmoonPlugin.instance, "${id.key}_daylightcycle"),
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

    var savedBloodmoonRemainingMillis: Double
        get() = Bukkit.getServer().profile.read(bloodmoonRemainingMillis)
        set(value) = Bukkit.getServer().profile.write(bloodmoonRemainingMillis, value)

    /**
     * The length in milliseconds of the bloodmoon.
     */
    val length: Long = config.getString("Length").toLong() * 1000

    val isIncreasing = config.getBool("Increasing")

    val bossbarEnabled: Boolean = config.getBool("Bossbar.Enabled")

    val createFog: Boolean = config.getBool("Bossbar.Fog")

    val darkenScreen: Boolean = config.getBool("Bossbar.DarkenScreen")

    val setDaylightCycle: Boolean = config.getBool("SetDaylightCycle")

    val setThunder: Boolean = config.getBool("SetThunder")

    val bossbarColor =
        BossBar.Color.entries.find { it.name == config.getString("Bossbar.Color") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Color")}")

    val bossbarStyle =
        BossBar.Overlay.entries.find { it.name == config.getString("Bossbar.Style") }
            ?: throw IllegalArgumentException("Invalid bossbar color: ${config.getString("Bossbar.Style")}")

    val usePrefix = config.getBool("UsePrefix")

    val activationMessage = config.getString("Messages.Activation")

    val deactivationMessage = config.getString("Messages.Deactivation")

    val activationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    val deactivationCommands = config.getStrings("Commands.Activation").map { it.replace("%world%", world.name) }

    override fun getID(): String = world.name

    var active: ActiveBloodmoon? = null
        private set

    /**
     * Whether the bloodmoon is currently activating.
     */
    var activating: Boolean = false

    open fun onActivation() {}

    open fun onDeactivation() {}

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
        announce: Boolean = true
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
//                        active = ActiveBloodmoon(this@BloodmoonWorld, length)
                        onActivation()
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
        announce: Boolean = true
    ) {
        active ?: throw IllegalStateException("Bloodmoon is not active.")
        val event = BloodmoonStopEvent(world, this, reason)
        event.callEvent()
        if (event.isCancelled()) {
            return
        }
        if (revertDaylightCycle) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        }
        deactivationCommands.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
        if (announce) {
            world.players.forEach { player ->
                player.sendMessage(deactivationMessage.miniToComponent())
            }
        }

        savedBloodmoonRemainingMillis = 0.0
        world.setStorm(false)
        if (setThunder) {
            world.clearWeatherDuration = 20 * 60 * 20
        }
        world.players.forEach { player ->
            active?.bossbar?.removeViewer(player)
        }
        active = null
        onDeactivation()
    }

    override fun onRemove() {
        active?.let { deactivate(StopCause.UNLOAD) }
    }
}
