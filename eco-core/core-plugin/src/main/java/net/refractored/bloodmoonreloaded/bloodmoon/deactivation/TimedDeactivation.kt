package net.refractored.bloodmoonreloaded.bloodmoon.deactivation

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
import net.refractored.bloodmoonreloaded.bloodmoon.activation.implementation.ActivationMethod
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import org.bukkit.Bukkit
import java.time.Duration

class TimedDeactivation(
    bloodmoonWorld: BloodmoonWorld,
) : ActivationMethod(bloodmoonWorld) {

    val configTime: Long = bloodmoonWorld.config.getString("length").toLong() * 1000

    private val timeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${bloodmoonWorld.world.name}_deactivation_timed_remaining"),
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
                "${bloodmoonWorld.world.name}_deactivation_timed_remaining_hours"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).first.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${bloodmoonWorld.world.name}_timed_remaining_minutes"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).second.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${bloodmoonWorld.world.name}_timed_remaining_seconds"
            ) {
                getHoursMinutesSeconds(getTimedRemaining()).third.toString()
            }
        )
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${bloodmoonWorld.world.name}_timed_remaining"
            ) {
                val time = getHoursMinutesSeconds(getTimedRemaining())
                "${time.first}h ${time.second}m ${time.third}s"
            }
        )
    }

    var endTime: Long = savedRemainingTime + System.currentTimeMillis()

    override fun getInfo(): ComponentLike {
        val timeframe: Duration = Duration.ofMillis(getTimedRemaining())
        return BloodmoonPlugin.instance.langYml
            .getStringPrefixed("messages.info.success.timed")
            .replace("%world%", bloodmoonWorld.world.name)
            .replace("%status%", this.bloodmoonWorld.status.miniMessage())
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

    fun saveRemainingTime() {
        savedRemainingTime = getTimedRemaining()
    }

    fun getTimedRemaining(): Long = (endTime - System.currentTimeMillis()).coerceAtLeast(0L)

    override fun onDeactivation() {
        savedRemainingTime = configTime
        endTime = configTime + System.currentTimeMillis()
    }

    override fun onActivation() {
        savedRemainingTime = -1L
        endTime = -1L
    }

    override fun periodicTasks() {
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) return
        saveRemainingTime()
    }

    override fun checkStatus(): Boolean {
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) return false
        if (endTime > System.currentTimeMillis()) return false
        return true
    }


}
