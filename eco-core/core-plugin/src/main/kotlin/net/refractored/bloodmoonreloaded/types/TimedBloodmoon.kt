package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
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

    val configTime: Long
        get() = config.getString("Timed").toLong() * 1000

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

    val startTime: Long = savedRemainingTime + System.currentTimeMillis()

    // Todo: Use a function to get the remaining time
    override fun getInfo(): ComponentLike {
        val timeframe: Duration = Duration.ofMillis(remainingTime)
        return BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-info-time")
                .replace("%world%", world.name)
                .replace("%status%", this.status.miniMessage())
                .replace("%hours%", timeframe.toHours().toString())
                .replace("%minutes%", timeframe.toMinutesPart().toString())
                .replace("%seconds%", timeframe.toSecondsPart().toString())
                .miniToComponent()
        }

    fun saveRemainingTime(){
        savedRemainingTime = (startTime - System.currentTimeMillis())
    }

    override fun onDeactivation() {
        savedRemainingTime = configTime
    }

    override fun periodicTasks() {
        saveRemainingTime()
    }

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        if (startTime > System.currentTimeMillis()) {
            return false
        }
        if (config.getBool("TimedNightOnly")) {
            return !world.isDayTime
        }
        return true
    }

}
