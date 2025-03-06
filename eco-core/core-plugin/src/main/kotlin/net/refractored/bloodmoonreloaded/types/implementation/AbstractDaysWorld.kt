package net.refractored.bloodmoonreloaded.types.implementation

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * This abstract class adds support for checking a world once a day if conditions are met.
 */
abstract class AbstractDaysWorld(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    private val lastDaytimeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${world.name}_last_daytime"),
            PersistentDataKeyType.BOOLEAN,
            true
        )

    /**
     * The last value of [World.isDayTime].
     */
    var lastDaytimeCheck: Boolean
        get() = Bukkit.getServer().profile.read(lastDaytimeKey)
        private set(value) = Bukkit.getServer().profile.write(lastDaytimeKey, value)

    /**
     * Should not be overridden.
     * @return true if the bloodmoon should activate.
     */
    final override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) return false

        if (world.isDayTime) {
            if (!lastDaytimeCheck) {
                onDaytime()
                lastDaytimeCheck = true
            }
            return false
        }

        if (lastDaytimeCheck && checkConditions()) {
            lastDaytimeCheck = false
            return true
        }

        onConditionFail()
        lastDaytimeCheck = false

        return false
    }

    /**
     * What to run once the world becomes day.
     */
    abstract fun onDaytime()

    /**
     * Conditions to check whenever the world turns from day to night.
     *
     * @return true if the bloodmoon should be activated that night.
     */
    abstract fun checkConditions(): Boolean

    /**
     * If [checkConditions] returns false, what should be run after.
     */
    open fun onConditionFail() {}

}
