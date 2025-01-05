package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.types.*
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.World.Environment
import java.nio.file.Files

/**
 * Registry for Bloodmoon worlds.
 */
object BloodmoonRegistry : ConfigCategory("worlds", "worlds") {

    private val registry = Registry<BloodmoonWorld>()

    fun isWorldEnabled(world: String): Boolean {
        val worldsList = BloodmoonPlugin.instance.configYml.getStrings("WorldsList")
        return if (BloodmoonPlugin.instance.configYml.getBool("Whitelist")) {
            worldsList.contains(world)
        } else {
            !worldsList.contains(world)
        }
    }

    fun getRegisteredWorlds() = registry.toList()

    // Get all worlds with the status of active
    fun getActiveWorlds() = registry.toList().filter { it.status == BloodmoonWorld.Status.ACTIVE }

    fun getWorld(id: String) = registry.get(id)

    fun unregisterWorld(id: String) {
        getWorld(id)?.let { world ->
            if (world.status == BloodmoonWorld.Status.ACTIVE) {
                world.deactivate(BloodmoonStopEvent.StopCause.UNLOAD)
            }
        }
        registry.remove(id)
    }

    fun registerWorld(world: BloodmoonWorld) = registry.register(world)

    override fun beforeReload(plugin: LibreforgePlugin) {
        // Check for worlds that don't have a config and create one.
        if (!plugin.dataFolder.resolve("worlds").exists()) {
            plugin.dataFolder.resolve("worlds").mkdir()
        }
        for (world in Bukkit.getWorlds()) {
            if (world.environment != Environment.NORMAL) continue
            if (!isWorldEnabled(world.name)) {
                return
            }
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

        if (!isWorldEnabled(id)) {
            return
        }

        when (config.getString("BloodmoonActivate").lowercase()) {
            "days" -> {
                registerWorld(DaysBloodmoon(world, config))
                return
            }
            "timed" -> {
                registerWorld(TimedBloodmoon(world, config))
                return
            }
            "chance" -> {
                registerWorld(ChanceBloodmoon(world, config))
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
