package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import net.refractored.bloodmoonreloaded.types.DaysBloodmoon
import net.refractored.bloodmoonreloaded.types.NoneBloodmoon
import net.refractored.bloodmoonreloaded.types.TimedBloodmoon
import org.bukkit.Bukkit
import org.bukkit.World.Environment
import java.nio.file.Files

object BloodmoonRegistry : ConfigCategory("worlds", "worlds") {
    private val registry = Registry<BloodmoonWorld>()

    fun getRegisteredWorlds() = registry.toList()

    fun getActiveWorlds() = registry.toList().filter { it.status == BloodmoonWorld.BloodmoonStatus.ACTIVE }

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
        config: Config
    ) {
        val world =
            Bukkit.getWorld(id) ?: run {
                BloodmoonPlugin.instance.logger.warning("World $id does not exist.")
                return
            }

        if (registry.find { it.world == world } != null) {
            BloodmoonPlugin.instance.logger.warning("World $id is already registered.")
            return
        }

        if (!config.getBool("enabled")) return

        when (config.getString("BloodmoonActivate").lowercase()) {
            "days" -> {
                registerWorld(DaysBloodmoon(world, config))
                return
            }
            "timed" -> {
                registerWorld(TimedBloodmoon(world, config))
                return
            }
            "none" -> {
                registerWorld(NoneBloodmoon(world, config))
                return
            }
        }

        BloodmoonPlugin.instance.logger.warning("Bloodmoon world $id has no valid BloodmoonActivate type.")
        registerWorld(NoneBloodmoon(world, config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}
