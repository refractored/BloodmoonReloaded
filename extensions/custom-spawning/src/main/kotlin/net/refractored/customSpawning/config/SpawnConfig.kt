package net.refractored.customSpawning.config

import com.willfp.eco.core.entities.Entities
import com.willfp.eco.core.entities.TestableEntity
import net.refractored.bloodmoonreloaded.extensions.Section
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import kotlin.random.Random

class SpawnConfig(
    val configSection: ConfigurationSection
): Section {
    private val worlds: List<World> = configSection.getStringList("worlds").mapNotNull { Bukkit.getWorld(it) }

    val mobs: List<Pair<Double, TestableEntity>>

    val strikeLightning
        get() = configSection.getBoolean("lightning")

    val replaceAllMobs
        get() = configSection.getBoolean("replace-all")

    init {

        if (worlds.isEmpty()) {
            throw IllegalArgumentException("No valid worlds found")
        }

        val tempMobs: MutableList<Pair<Double, TestableEntity>> = mutableListOf()

        for (string in configSection.getStringList("mobs")) {
            val splitString = string.split(",", limit = 2)
            tempMobs.add(Pair(splitString[0].toDouble(), Entities.lookup(splitString[1])))
        }

        mobs = tempMobs.toList()
    }

    override fun getWorlds(): List<World> {
        return worlds
    }

    /**
     * Spawn a horde at a player's location
     */
    fun getWeightedMob(): TestableEntity {
        val totalWeight = mobs.sumOf { it.first }

        val randomValue = Random.nextDouble(0.0, totalWeight)

        var cumulativeWeight = 0.0
        for ((weight, entity) in mobs) {
            cumulativeWeight += weight
            if (randomValue <= cumulativeWeight) {
                return entity
            }
        }

        // Should usually never reach here
        return mobs.random().second
    }
}
