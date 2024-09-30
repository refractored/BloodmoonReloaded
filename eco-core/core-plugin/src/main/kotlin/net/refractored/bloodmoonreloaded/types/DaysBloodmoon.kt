package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * Represents a bloodmoon that is activated after a certain amount of in-game days.
 */
class DaysBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    private val dayCountKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_day_count"),
            PersistentDataKeyType.INT,
            0
        )

    var dayCount: Int
        get() = Bukkit.getServer().profile.read(dayCountKey)
        private set(value) = Bukkit.getServer().profile.write(dayCountKey, value)

    private val daysUntilActivation: Int
        get() = config.getInt("Days")

    /**
     * The last value of [World.isDayTime] two ticks ago.
     */
    private var lastDaytimeCheck: Boolean = false

    override fun shouldActivate(): Boolean {
        if (status != BloodmoonStatus.INACTIVE) {
            return false
        }
        if (world.isDayTime && !lastDaytimeCheck) {
            lastDaytimeCheck = true
            dayCount--
            return false
        }
        if (!world.isDayTime) {
            lastDaytimeCheck = false
        }
        if (dayCount >= 0) {
            return false
        }
        return true
    }

    override fun onActivation() {
        dayCount = daysUntilActivation
    }
}
