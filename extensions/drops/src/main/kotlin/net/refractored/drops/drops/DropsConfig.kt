package net.refractored.drops.drops

import com.willfp.eco.core.entities.Entities
import com.willfp.eco.core.entities.TestableEntity
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.TestableItem
import net.refractored.drops.drops.DropsConfig.HordeType
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import kotlin.collections.get
import kotlin.text.get

data class DropsConfig(
    val configSection: ConfigurationSection
) {
    val worlds: List<World> = configSection.getStringList("worlds").mapNotNull { Bukkit.getWorld(it) }

    val drops: List<Drop> = getItems()

    val entities: List<TestableEntity>? =
        if (configSection.getBoolean("mob-whitelist.enabled")) {
            configSection.getStringList("mob-whitelist.mobs").map {
                Entities.lookup(it)
            }
        } else {
            null
        }

    init {
        if (worlds.isEmpty()) {
            throw IllegalArgumentException("No valid worlds found")
        }

        if (drops.isEmpty()) {
            throw IllegalArgumentException("No valid mobs found")
        }
    }

    fun getItems(): List<Drop> {
        val dropsList = configSection.getMapList("drops")

        val dropItems = mutableListOf<Drop>()

        // Iterate through the drops
        for (drop in dropsList) {
            if (drop is Map<*, *>) {
                val weight = drop["weight"] as? Int ?: continue
                val item = drop["item"] as? String ?: "unknown"

                val value =
                    when (val hordesValue = drop["hordes"]) {
                        is Boolean -> if (hordesValue) HordeType.ALLOWED else HordeType.BLOCKED
                        is String ->
                            when (hordesValue) {
                                "true" -> HordeType.ALLOWED
                                "false" -> HordeType.BLOCKED
                                "whitelist" -> HordeType.WHITELIST
                                else -> throw IllegalArgumentException("Invalid horde type \"$hordesValue\"")
                            }
                        else -> throw IllegalArgumentException("Invalid horde type \"$hordesValue\"")
                    }

                dropItems.add(
                    Drop(
                        weight = weight,
                        hordes = value,
                        testableItem = Items.lookup(item)
                    )
                )
            }
        }

        return dropItems
    }

    enum class HordeType {
        ALLOWED,
        BLOCKED,

        /**
         * Hordes only
         */
        WHITELIST
    }

    /**
     * Tries to get a random itemstack from the drops.
     * @return an ItemStack from the drops, or null if no itemstack met the criteria
     */
    fun tryRandomItemstack(hordes: Boolean): ItemStack? {
        val totalWeight = drops.sumOf { it.weight }
        val randomValue = Math.random() * totalWeight
        var accumulatedWeight = 0.0

        for (drop in drops) {
            accumulatedWeight += drop.weight
            if (randomValue <= accumulatedWeight) {
                when (drop.hordes) {
                    HordeType.BLOCKED -> if (hordes) continue
                    HordeType.WHITELIST -> if (!hordes) continue
                    HordeType.ALLOWED -> {}
                }
                return drop.testableItem.item
            }
        }
        return null
    }

    /**
     * Gets a random itemstack from the drops.
     * @return an ItemStack from the drops
     */
    fun getRandomItemstack(hordes: Boolean): ItemStack = tryRandomItemstack(hordes) ?: drops
        .filter {
            when (it.hordes) {
                HordeType.BLOCKED -> !hordes
                HordeType.WHITELIST -> hordes
                HordeType.ALLOWED -> true
            }
        }.random()
        .testableItem.item
}

data class Drop(
    val weight: Int,
    val hordes: HordeType,
    val testableItem: TestableItem
)
