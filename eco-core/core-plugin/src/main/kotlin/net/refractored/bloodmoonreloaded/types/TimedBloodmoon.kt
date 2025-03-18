package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import org.bukkit.Bukkit
import org.bukkit.World
import java.time.Duration

/**
 * Represents a bloodmoon that is activated after a certain amount of time.
 */
class TimedBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    val configTime: Long = config.getString("Timed").toLong() * 1000

    private val timeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${world.name}_timed_remaining"),
            PersistentDataKeyType.DOUBLE,
            // For anyone wondering why I did this.
            // Eco doesnt have support for Longs for some reason :c
            configTime.toDouble()
        )

    private var savedRemainingTime: Long
        get() = Bukkit.getServer().profile.read(timeKey).toLong()
        set(value) = Bukkit.getServer().profile.write(timeKey, value.toDouble())

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_timed_remaining_hours"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).first.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_timed_remaining_minutes"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).second.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_timed_remaining_seconds"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).third.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_timed_remaining"
            ) {
                val time = getHoursMinutesSeconds(getTimedRemaining())
                "${time.first}h ${time.second}m ${time.third}s"
            }
        )
    }

    var startTime: Long = savedRemainingTime + System.currentTimeMillis()

    override fun getInfo(): ComponentLike {
        val timeframe: Duration = Duration.ofMillis(getTimedRemaining())
        return BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-info-time")
                .replace("%world%", world.name)
                .replace("%status%", this.status.miniMessage())
                .replace("%hours%", timeframe.toHours().toString())
                .replace("%minutes%", timeframe.toMinutesPart().toString())
                .replace("%seconds%", timeframe.toSecondsPart().toString())
                .miniToComponent()
        }

    fun getHoursMinutesSeconds(durationMillis: Long): Triple<Long, Long, Long> {
        val hours = durationMillis / 3600000
        val minutes = (durationMillis % 3600000) / 60000
        val seconds = (durationMillis % 60000) / 1000
        return Triple(hours, minutes, seconds)
    }

    fun saveRemainingTime(){
        savedRemainingTime = getTimedRemaining()
    }

    fun getTimedRemaining(): Long {
        return (startTime - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    override fun onDeactivation() {
        savedRemainingTime = configTime
        startTime = configTime + System.currentTimeMillis()
    }

    override fun onActivation() {
        savedRemainingTime = -1L
        startTime = -1L
    }

    override fun periodicTasks() {
        if (status != Status.INACTIVE) return
        saveRemainingTime()
    }

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) return false
        if (startTime > System.currentTimeMillis()) return false

        if (config.getBool("TimedNightOnly")){
            return !world.isDayTime
        }

        return true
    }



    companion object : TypeRegistry.BloodmoonWorldFactory {
        override fun create(world: World, config: Config): BloodmoonWorld {
            return TimedBloodmoon(world, config)
        }
    }

}
