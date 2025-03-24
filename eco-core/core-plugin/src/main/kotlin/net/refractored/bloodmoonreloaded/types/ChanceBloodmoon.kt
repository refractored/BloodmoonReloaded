package net.refractored.bloodmoonreloaded.types

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import net.refractored.bloodmoonreloaded.types.implementation.AbstractDaysWorld
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
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

    override fun getInfo(): ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.info.success.chance")
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
        chance = config.getDouble("chance.initial-percentage").coerceAtMost(1.0)
    }

    override fun onDaytime() {
        return
    }

    override fun checkConditions(): Boolean = Random.nextDouble(1.0) < chance

    override fun onConditionFail() {
        if (!config.getBool("chance.increment.enabled")) return
        chance += (Random.nextDouble(config.getDouble("chance.increment.min"), config.getDouble("chance.increment.max")))
    }

    companion object : TypeRegistry.BloodmoonWorldFactory {
        override fun create(world: World, config: Config): BloodmoonWorld = ChanceBloodmoon(world, config)
    }
}
