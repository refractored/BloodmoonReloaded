package net.refractored.bloodmoonreloaded.types.activation.implementation

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * This abstract class adds support for checking a world once a day if conditions are met.
 */
abstract class AbstractDayActivation(
    bloodmoonWorld: BloodmoonWorld,
) : ActivationMethod(bloodmoonWorld) {

    private val lastDaytimeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${bloodmoonWorld.world.name}_last_daytime"),
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
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) return false

        if (bloodmoonWorld.world.isDayTime) {
            handleDaytime()
            return false
        }

        return handleNighttime()
    }

    private fun handleDaytime() {
        if (!lastDaytimeCheck) {
            onDaytime()
            lastDaytimeCheck = true
        }
    }

    private fun handleNighttime(): Boolean {
        if (!lastDaytimeCheck) return false

        lastDaytimeCheck = false

        if (checkConditions()) {
            return true
        }

        onConditionFail()
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
