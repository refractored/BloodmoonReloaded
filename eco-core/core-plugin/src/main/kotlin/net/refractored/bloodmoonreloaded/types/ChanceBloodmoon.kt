package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.implementation.AbstractDaysWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.random.Random

/**
 * Represents a world that will have a chance of a bloodmoon every night.
 */
class ChanceBloodmoon(
    world: World,
    config: Config
) : AbstractDaysWorld(world, config) {

    private val chanceKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${id.key}_chance"),
            PersistentDataKeyType.DOUBLE,
            config.getDouble("Chance").coerceAtMost(1.0)
        )

    var chance: Double
        get() = Bukkit.getServer().profile.read(chanceKey)
        private set(value) = Bukkit.getServer().profile.write(chanceKey, value)

    override fun getInfo(): ComponentLike =
        BloodmoonPlugin.instance.langYml
            .getStringPrefixed("messages.bloodmoon-info-chance")
            .replace("%world%", world.name)
            .replace("%status%", this.status.miniMessage())
            .replace("%chance%", this.fancyChance)
            .miniToComponent()

    /**
     * @return The chance as a percentage out of 100.
     */
    val fancyChance: String
        get() = "%.2f".format(chance * 100)

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${world.name}_chance"
            ) {
                fancyChance
            }
        )
    }

    override fun onActivation() {
        chance = config.getDouble("Chance").coerceAtMost(1.0)
    }

    override fun onDaytime() {
        return
    }

    override fun checkConditions(): Boolean {
        return Random.nextDouble(1.0) < chance
    }

    override fun onConditionFail() {
        if (!config.getBool("ChanceIncrementEnabled")) return
        chance += (Random.nextDouble(config.getDouble("ChanceIncrementMin"), config.getDouble("ChanceIncrementMax")))
    }
}
