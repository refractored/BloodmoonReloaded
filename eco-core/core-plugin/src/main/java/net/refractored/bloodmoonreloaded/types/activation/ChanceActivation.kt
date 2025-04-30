package net.refractored.bloodmoonreloaded.types.activation

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.types.activation.implementation.AbstractDayActivation
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.Bukkit
import kotlin.random.Random

/**
 * Represents a world that will have a chance of a bloodmoon every night.
 */
class ChanceActivation(
    bloodmoonWorld: BloodmoonWorld,
) : AbstractDayActivation(bloodmoonWorld) {

    private val chanceKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${bloodmoonWorld.id.key}_chance"),
            PersistentDataKeyType.DOUBLE,
            bloodmoonWorld.config.getDouble("chance.initial-percentage").coerceAtMost(1.0)
        )

    var chance: Double
        get() = Bukkit.getServer().profile.read(chanceKey)
        private set(value) = Bukkit.getServer().profile.write(chanceKey, value)

    override fun getInfo(): ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.info.success.chance")
        .replace("%world%", bloodmoonWorld.world.name)
        .replace("%status%", bloodmoonWorld.status.miniMessage())
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
                "${bloodmoonWorld.world.name}_chance"
            ) {
                fancyChance
            }
        )
    }

    override fun onActivation() {
        chance = bloodmoonWorld.config.getDouble("chance.initial-percentage").coerceAtMost(1.0)
    }

    override fun onDaytime() {
        return
    }

    override fun checkConditions(): Boolean = Random.nextDouble(1.0) < chance

    override fun onConditionFail() {
        if (!bloodmoonWorld.config.getBool("chance.increment.enabled")) return
        chance += (Random.nextDouble(bloodmoonWorld.config.getDouble("chance.increment.min"), bloodmoonWorld.config.getDouble("chance.increment.max")))
    }
}
