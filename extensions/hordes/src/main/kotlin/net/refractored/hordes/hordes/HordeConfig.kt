package net.refractored.hordes.hordes

import com.willfp.eco.core.entities.Entities
import com.willfp.eco.core.entities.TestableEntity
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.extensions.Section
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

data class HordeConfig(
    val configSection: ConfigurationSection
): Section {
    val pdcKey = NamespacedKey(BloodmoonPlugin.instance, "horde-${configSection.name}")

    private val worlds: List<World> = configSection.getStringList("Worlds").mapNotNull { Bukkit.getWorld(it) }

    val mobs: List<TestableEntity>

    val strikeLightning
        get() = configSection.getBoolean("strike-lightning")

    val maxMobs
        get() = configSection.getInt("max-size")

    val minMobs
        get() = configSection.getInt("min-size")

    val minTickTime
        get() = configSection.getLong("spawn-rate-ticks-min")

    val maxTickTime
        get() = configSection.getLong("spawn-rate-ticks-max")

    val maxY
        get() = configSection.getDouble("max-y")

    val spawnDistance
        get() = configSection.getInt("spawn-distance")

    init {

        if (worlds.isEmpty()) {
            throw IllegalArgumentException("No valid worlds found in ${configSection.name}")
        }

        mobs = configSection.getStringList("Mobs").map { Entities.lookup(it) }

        if (mobs.isEmpty()) {
            throw IllegalArgumentException("No valid mobs found in ${configSection.name}")
        }
    }

    /**
     * Spawn a horde at a player's location
     */
    fun spawnHorde(
        player: Player,
        /**
         * Whether to announce the horde spawn to all players in the world
         * This wil be disabled either way if the config is set to false.
         */
        announce: Boolean = true
    ) {
        val spawnAmount = (minMobs..maxMobs).random()

        for (i in 0 until spawnAmount) {
            val mob = mobs.random()

            val mobLocation: Location = player.location.clone()

            mobLocation.x += ((spawnDistance.unaryMinus())..spawnDistance).random()

            mobLocation.z += ((spawnDistance.unaryMinus())..spawnDistance).random()

            mobLocation.y =
                (
                    mobLocation.world
                        .getHighestBlockAt(mobLocation)
                        .location.y + 1
                    ).coerceAtMost(maxY)

            val entity = mob.spawn(mobLocation)

            entity.persistentDataContainer.set(pdcKey, PersistentDataType.BYTE, 1)

            if (strikeLightning) {
                player.world.strikeLightningEffect(mobLocation)
            }
        }

        if (!configSection.getBoolean("broadcast.enabled") || !announce) return

        player.world.players.forEach {
            it.sendMessage(
                (configSection.getString("broadcast.message") ?: "")
                    .replace("%player%", player.name)
                    .miniToComponent()
            )
        }
    }

    override fun getWorlds(): List<World> {
        return worlds
    }
}
