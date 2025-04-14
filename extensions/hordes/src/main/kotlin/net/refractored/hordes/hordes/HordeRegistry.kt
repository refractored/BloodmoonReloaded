package net.refractored.hordes.hordes

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.hordes.HordesExtension
import org.bukkit.World

object HordeRegistry {
    @JvmStatic
    private val registeredConfigs = mutableListOf<HordeConfig>()

    /**
     * Gets a preset from the loaded presets.
     * @param world The world to get the preset for.
     * @return The HordeConfig for the preset, or null if it does not exist
     */
    @JvmStatic
    fun getHordeConfig(world: World): HordeConfig? = registeredConfigs.find { it.worlds.contains(world) }

    /**
     * Gets a read-only map of all the loaded configs.
     * @return The mutable preset list
     */
    @JvmStatic
    fun getHordeConfigs() = registeredConfigs.toList()

    /**
     * Create a new preset and adds it to the loaded configs
     * @param config The HordeConfig to save a config for
     */
    @JvmStatic
    fun createPreset(config: HordeConfig) {
        for (world in config.worlds) {
            if (registeredConfigs.any { it.worlds.contains(world) }) {
                throw IllegalArgumentException("Multiple presets cannot be applied to the same world!")
            }
        }
        registeredConfigs.add(config)
    }

    /**
     * Removes a config from the loaded configs
     * @param config The config of the preset
     */
    @JvmStatic
    fun removePreset(config: HordeConfig) {
        registeredConfigs.remove(config)
    }

    /**
     * Deletes all loaded configs and populates it with the configuration in the hordes.yml.
     */
    @JvmStatic
    fun refreshHordeConfigs() {
        registeredConfigs.clear()
        val config = HordesExtension.instance.hordeConfig
        val section = config.getConfigurationSection("")
        val keys = section!!.getKeys(false)
        if (keys.isEmpty()) return
        for (key in keys) {
            try {
                config.getConfigurationSection(key)?.let { sectionKey ->
                    createPreset(HordeConfig(sectionKey))
                } ?: continue
            } catch (e: Exception) {
                BloodmoonPlugin.instance.logger.severe("Failed to load horde config \"$key\":")
                BloodmoonPlugin.instance.logger.severe("${e.message}")
                if (BloodmoonPlugin.instance.configYml.getBool("debug")) e.printStackTrace()
                continue
            }
        }
    }
}
