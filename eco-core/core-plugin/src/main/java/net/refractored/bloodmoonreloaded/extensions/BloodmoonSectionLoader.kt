package net.refractored.bloodmoonreloaded.extensions

import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.BloodmoonPlugin.Companion.logSevere
import org.bukkit.World
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection

class BloodmoonSectionLoader
<T : Section>(
    private val configuration: Configuration,
    private val extension: Extension,
    private val sectionFactory: (ConfigurationSection) -> T,
) : ConfigSectionLoader<T>{

    private val registeredConfigs = mutableListOf<T>()

    override fun removeConfig(section: T) {
        removeConfig(section)
    }

    override fun createConfig(section: T) {
        registeredConfigs.add(section)
    }

    override fun getSections(): List<T> {
        return registeredConfigs.toList()
    }

    override fun getSection(world: World): T? {
        return registeredConfigs.find { it.worlds.contains(world)}
    }

    override fun refreshConfigs() {
        registeredConfigs.clear()
        val section = configuration.getConfigurationSection("")
        val keys = section!!.getKeys(false)
        if (keys.isEmpty()) return
        for (key in keys) {
            try {
                configuration.getConfigurationSection(key)?.let { sectionKey ->
                    createConfig(sectionFactory(sectionKey))
                } ?: continue
            } catch (e: Exception) {
                extension.logSevere("Failed to load config section \"$key\": ${e.message}")
                if (BloodmoonPlugin.instance.configYml.getBool("debug")) e.printStackTrace()
                continue
            }
        }
    }


}
