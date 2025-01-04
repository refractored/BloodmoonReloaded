package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.random.Random

/**
 * Represents a world that will have a chance of a bloodmoon every night.
 */
class ChanceBloodmoon(
    world: World,
    config: Config
) : BloodmoonWorld(world, config) {

    private val lastDaytimeKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_last_daytime"),
            PersistentDataKeyType.BOOLEAN,
            true
        )

    private val chanceKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_chance"),
            PersistentDataKeyType.DOUBLE,
            config.getDouble("IncrementChanceStart").coerceAtMost(1.0)
        )

    var chance: Double
        get() = Bukkit.getServer().profile.read(chanceKey)
        private set(value) = Bukkit.getServer().profile.write(chanceKey, value)

    /**
     * @return The chance as a percentage out of 100.
     */
    var fancyChance: String
        get() = (chance * 100).toString()
        private set(_) {}

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${id}_chance"
            ) {
                fancyChance
            }
        )
    }

    /**
     * @return false
     */
    var lastDaytimeCheck: Boolean
        get() = Bukkit.getServer().profile.read(lastDaytimeKey)
        private set(value) = Bukkit.getServer().profile.write(lastDaytimeKey, value)

    override fun shouldActivate(): Boolean {
        if (status != Status.INACTIVE) {
            return false
        }

        if (world.isDayTime) {
            if (!lastDaytimeCheck) {
                lastDaytimeCheck = true
            }
            return false
        }

        if (lastDaytimeCheck && Random.nextDouble(1.0) < chance) {
            lastDaytimeCheck = false
            return true
        }

        if (config.getBool("ChanceIncrementEnabled")) {
                chance += Random.nextDouble(config.getDouble("ChanceIncrementMin"), config.getDouble("ChanceIncrementMax"))
        }

        lastDaytimeCheck = false
        return false
    }

    override fun onActivation() {
        chance = config.getDouble("Chance").coerceAtMost(1.0)
    }
}
