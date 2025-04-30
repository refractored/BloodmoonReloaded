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

/**
 * Represents a bloodmoon that is activated after a certain amount of in-game days.
 */
class DaysActivation(
    bloodmoonWorld: BloodmoonWorld,
) : AbstractDayActivation(bloodmoonWorld) {

    private val dayCountKey =
        PersistentDataKey(
            BloodmoonPlugin.instance.namespacedKeyFactory.create("${bloodmoonWorld.world.name}_day_count"),
            PersistentDataKeyType.INT,
            0
        )

    var dayCount: Int
        get() = Bukkit.getServer().profile.read(dayCountKey)
        set(value) = Bukkit.getServer().profile.write(dayCountKey, value)

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                BloodmoonPlugin.instance,
                "${bloodmoonWorld.world.name}_days_remaining"
            ) {
                dayCount.toString()
            }
        )
    }

    override fun getInfo(): ComponentLike = BloodmoonPlugin.instance.langYml
        .getStringPrefixed("messages.info.success.days")
        .replace("%world%", bloodmoonWorld.world.name)
        .replace("%status%", this.bloodmoonWorld.status.miniMessage())
        .replace("%days%", this.dayCount.toString())
        .miniToComponent()

    private val daysUntilActivation: Int
        get() = bloodmoonWorld.config.getInt("days.count")

    override fun onActivation() {
        dayCount = daysUntilActivation
    }

    override fun onDaytime() {
        dayCount--
    }

    override fun checkConditions(): Boolean = (dayCount <= 0)
}
