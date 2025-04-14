package net.refractored.customSpawning.config

import net.refractored.customSpawning.CustomSpawningExtension
import org.bukkit.World

object SpawnConfigRegistry {
    @JvmStatic
    private val registeredConfigs = mutableListOf<SpawnConfig>()

    /**
     * Gets a preset from the loaded presets
     * @param world The world to get the preset for
     * @return The SpawnConfig for the preset, or null if it does not exist
     */
    @JvmStatic
    fun getSpawnConfig(world: World): SpawnConfig? = registeredConfigs.find { it.worlds.contains(world) }

    /**
     * Gets a read-only map of all the presets loaded.
     * @return The mutable preset list
     */
    @JvmStatic
    fun getSpawnConfigs() = registeredConfigs.toList()

    /**
     * Create a new preset and adds it to the config & map
     * @param config The SpawnConfig to add
     */
    @JvmStatic
    fun createPreset(config: SpawnConfig) {
        for (world in config.worlds) {
            if (registeredConfigs.any { it.worlds.contains(world) }) {
                throw IllegalArgumentException("Multiple presets cannot be applied to the same world!")
            }
        }
        registeredConfigs.add(config)
    }

    /**
     * Removes a preset from the config & map
     * @param config The SpawnConfig to remove
     */
    @JvmStatic
    fun removePreset(config: SpawnConfig) {
        registeredConfigs.remove(config)
    }

    /**
     * Deletes all presets in the map and populates it with the presets in the config.
     */
    @JvmStatic
    fun refreshSpawnConfigs() {
        registeredConfigs.clear()
        val config = CustomSpawningExtension.instance.spawnConfigYml
        val section = config.getConfigurationSection("Presets")
        val keys = section!!.getKeys(false)
        if (keys.isEmpty()) return
        for (key in keys) {
            config.getConfigurationSection(key)?.let { sectionKey ->
                createPreset(SpawnConfig(sectionKey))
            } ?: continue
        }
    }
}
