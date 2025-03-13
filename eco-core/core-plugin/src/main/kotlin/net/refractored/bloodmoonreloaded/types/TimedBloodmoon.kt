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

    // TODO: This may cause milliseconds of time to be lost
    private val timeframe: Duration = Duration.ofMillis(remainingTime)


    override var info: ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.bloodmoon-info-time")
        .replace("%world%", world.name)
        .replace("%status%", this.status.miniMessage())
        .replace("%hours%", timeframe.toHours().toString())
        .replace("%minutes%", timeframe.toMinutesPart().toString())
        .replace("%seconds%", timeframe.toSecondsPart().toString())
        .miniToComponent()

    private val timeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${world.name}_remaining_time"),
            PersistentDataKeyType.DOUBLE,
            0.0
        )

    private var remainingMilis: Long
        get() = Bukkit.getServer().profile.read(timeKey).toLong()
        set(value) = Bukkit.getServer().profile.write(timeKey, value.toDouble())

    val millisUntilActivation: Long = if (remainingMilis == 0L) {
        config.getString("Time").toLong() * 1000
    } else {
        remainingMilis.toLong()
    }

    private val activationTime = System.currentTimeMillis() + millisUntilActivation

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        remainingMilis = (activationTime - System.currentTimeMillis())
        if (activationTime < System.currentTimeMillis()) {
            return false
        }
        if (config.getBool("TimedNightOnly")) {
            return !world.isDayTime
        }
        return true
    }

    override fun onActivation() {
        remainingMilis = 0L
    }
}
