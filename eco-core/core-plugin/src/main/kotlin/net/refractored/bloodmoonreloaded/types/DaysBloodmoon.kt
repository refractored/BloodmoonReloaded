package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * Represents a bloodmoon that is activated after a certain amount of in-game days.
 */
class DaysBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_days_remaining"
            ) {
                dayCount.toString()
            }
        )
    }

    override var info = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.bloodmoon-info-days")
        .replace("%world%", world.name)
        .replace("%status%", this.status.toString())
        .replace("%days%", this.dayCount.toString())
        .miniToComponent()

    private val dayCountKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_day_count"),
            PersistentDataKeyType.INT,
            0
        )

    private val lastDaytimeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_last_daytime"),
            PersistentDataKeyType.BOOLEAN,
            true
        )

    var dayCount: Int
        get() = Bukkit.getServer().profile.read(dayCountKey)
        private set(value) = Bukkit.getServer().profile.write(dayCountKey, value)

    private val daysUntilActivation: Int
        get() = config.getInt("Days")

    /**
     * The last value of [World.isDayTime] two ticks ago.
     */
    var lastDaytimeCheck: Boolean
        get() = Bukkit.getServer().profile.read(lastDaytimeKey)
        private set(value) = Bukkit.getServer().profile.write(lastDaytimeKey, value)

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) return false

        if (world.isDayTime) {
            if (!lastDaytimeCheck) {
                lastDaytimeCheck = true
                dayCount--
            }
            return false
        } else {
            lastDaytimeCheck = false
        }

        if (dayCount > 0) return false

        return !world.isDayTime
    }

    override fun onActivation() {
        dayCount = daysUntilActivation
    }
}
