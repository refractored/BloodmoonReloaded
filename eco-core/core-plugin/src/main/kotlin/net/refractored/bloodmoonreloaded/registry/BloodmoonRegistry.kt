package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.types.*
import net.refractored.bloodmoonreloaded.types.abstract.BloodmoonWorld
import org.bukkit.Bukkit
import java.nio.file.Files

/**
 * Registry for Bloodmoon worlds.
 */
object BloodmoonRegistry : ConfigCategory("worlds", "worlds") {

    private val registry = Registry<BloodmoonWorld>()

    /**
     * This method automatically checks if the world is blacklisted, or whitelisted.
     * @return True if the world is whitelisted/not blacklisted, or false if the world isn't whitelisted/is blacklisted
     */
    fun isWorldEnabled(world: String): Boolean {
        val worldsList = BloodmoonPlugin.instance.configYml.getStrings("WorldsList")
        return if (BloodmoonPlugin.instance.configYml.getBool("Whitelist")) {
            if (worldsList.isEmpty()) {
                BloodmoonPlugin.instance.logger.warning("WorldsList is empty. No worlds will be enabled!")
            }
            worldsList.contains(world)
        } else {
            !worldsList.contains(world)
        }
    }

    fun getWorlds() = registry.toList()

    // Get all worlds with the status of active
    fun getActiveWorlds() = registry.filter { it.status == BloodmoonWorld.Status.ACTIVE }

    fun getWorld(id: String) = registry.get(id)

    /**
     * This method automatically
     */
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
        val worldsDir = plugin.dataFolder.resolve("worlds")
        if (!worldsDir.exists()) {
            worldsDir.mkdir()
        }

        for (world in Bukkit.getWorlds()) {
            if (!isWorldEnabled(world.name)) continue
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
        // Each config file is checked inside of the worlds folder,
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
            "mirror" -> {
                registerWorld(MirrorBloodmoon(world, config))
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
