package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World

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

    private var remainingTime: Double
        get() = Bukkit.getServer().profile.read(timeKey)
        set(value) = Bukkit.getServer().profile.write(timeKey, value)

    private val millisUntilActivation: Long = if (remainingTime == 0.0) {
        config.getString("Time").toLong() * 1000
    } else {
        remainingTime.toLong()
    }

    private val activationTime = System.currentTimeMillis() + millisUntilActivation

    override fun shouldActivate(): Boolean {
        remainingTime = (activationTime - System.currentTimeMillis()).toDouble()
        if (active != null) {
            return false
        }
        if (activationTime < System.currentTimeMillis()) {
            return false
        }
        if (world.isDayTime) {
            return false
        }
        return true
    }

    override fun onActivation() {
        remainingTime = 0.0
    }
}
