package net.refractored.discord.discord

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.discord.DiscordExtension
import org.bukkit.World

object DiscordRegistry {
    @JvmStatic
    private val registeredConfigs = mutableListOf<DiscordConfig>()

    /**
     * Gets a preset from the loaded presets.
     * @param world The world to get the preset for.
     * @return The HordeConfig for the preset, or null if it does not exist
     */
    @JvmStatic
    fun getConfig(world: World): DiscordConfig? = registeredConfigs.find { it.worlds.contains(world) }

    /**
     * Gets a read-only map of all the loaded configs.
     * @return The mutable preset list
     */
    @JvmStatic
    fun getConfigs() = registeredConfigs.toList()

    /**
     * Create a new preset and adds it to the loaded configs
     * @param config The HordeConfig to save a config for
     */
    @JvmStatic
    fun createPreset(config: DiscordConfig) {
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
    fun removePreset(config: DiscordConfig) {
        registeredConfigs.remove(config)
    }

    /**
     * Deletes all loaded configs and populates it with the configuration in the hordes.yml.
     */
    @JvmStatic
    fun refreshConfigs() {
        registeredConfigs.clear()
        val config = DiscordExtension.instance.config
        val section = config.getConfigurationSection("")
        val keys = section!!.getKeys(false)
        if (keys.isEmpty()) return
        for (key in keys) {
            try {
                config.getConfigurationSection(key)?.let { sectionKey ->
                    createPreset(DiscordConfig(sectionKey))
                } ?: continue
            } catch (e: Exception) {
                BloodmoonPlugin.instance.logger.severe("Failed to load drops config \"$key\": ${e.message}")
                if (BloodmoonPlugin.instance.configYml.getBool("debug")) e.printStackTrace()
                continue
            }
        }
    }
}
