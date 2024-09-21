package net.refractored.bloodmoonreloaded.worlds

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.Bukkit
import org.bukkit.World.Environment
import java.nio.file.Files

object BloodmoonRegistry : ConfigCategory("worlds", "worlds") {
    private val registry = Registry<BloodmoonWorld>()

    fun getRegisteredWorlds() = registry.toList()

    fun getActiveWorlds() = registry.toList().filter { it.active != null }

    fun getWorld(id: String) = registry[id]

    fun unregisterWorld(id: String) = registry.remove(id)

    fun registerWorld(world: BloodmoonWorld) = registry.register(world)

    /**
     * Check for worlds that don't have a config and create one.
     */
    override fun beforeReload(plugin: LibreforgePlugin) {
        if (!plugin.dataFolder.resolve("worlds").exists()) {
            plugin.dataFolder.resolve("worlds").mkdir()
        }
        for (world in Bukkit.getWorlds()) {
            if (world.environment != Environment.NORMAL) continue
            if (plugin.dataFolder.resolve("worlds/${world.name}.yml").exists()) continue
            plugin.getResource("DefaultWorldConfig.yml").use {
                Files.copy(it!!, plugin.dataFolder.resolve("worlds/${world.name}.yml").toPath())
            }
        }
    }

    override fun acceptConfig(
        plugin: LibreforgePlugin,
        id: String,
        config: Config,
    ) {
        if (!config.getBool("enabled")) return
        val world =
            Bukkit.getWorld(id) ?: run {
                BloodmoonPlugin.instance.logger.warning("World $id does not exist.")
                return
            }
        world.gameRules.clone()
        registerWorld(BloodmoonWorld(world, config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}
