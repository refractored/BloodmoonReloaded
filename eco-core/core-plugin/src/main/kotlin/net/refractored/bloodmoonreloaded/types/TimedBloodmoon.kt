package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * Represents a bloodmoon that is activated after a certain amount of time.
 */
class TimedBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    private val timeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_remaining_time"),
            PersistentDataKeyType.DOUBLE,
            0.0
        )

    private var remainingMilis: Double
        get() = Bukkit.getServer().profile.read(timeKey)
        set(value) = Bukkit.getServer().profile.write(timeKey, value)

    val millisUntilActivation: Long = if (remainingMilis == 0.0) {
        config.getString("Time").toLong() * 1000
    } else {
        remainingMilis.toLong()
    }

    private val activationTime = System.currentTimeMillis() + millisUntilActivation

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }
        remainingMilis = (activationTime - System.currentTimeMillis()).toDouble()
        if (activationTime < System.currentTimeMillis()) {
            return false
        }
        return !world.isDayTime
    }

    override fun onActivation() {
        remainingMilis = 0.0
    }
}
