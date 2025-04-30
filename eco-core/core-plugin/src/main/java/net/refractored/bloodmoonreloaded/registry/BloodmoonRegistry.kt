@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.config.readConfig
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.loader.internal.configs.RegistrableConfig
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
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
        val worldsList = BloodmoonPlugin.instance.configYml.getStrings("worlds-list")
        if (world.startsWith("_")) return false
        return if (BloodmoonPlugin.instance.configYml.getBool("whitelist")) {
            if (worldsList.isEmpty()) {
                BloodmoonPlugin.instance.logger.warning("worlds-list is empty. No worlds will be enabled!")
            }
            worldsList.contains(world)
        } else {
            !worldsList.contains(world)
        }
    }

    fun getWorlds() = registry.toList()

    // Get all worlds with the status of active
    fun getActiveWorlds() = registry.filter { it.status == BloodmoonWorld.Status.ACTIVE /* && it !is NoneBloodmoon */}

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

    private fun addToRegistry(world: BloodmoonWorld) = registry.register(world)

    /**
     * This method automatically creates a world config file.
     * @param subDirectory The subdirectory to create the world config in.
     * @param world The world to create the config for.
     * @throws IllegalStateException If the world config already exists.
     */
    fun createWorldConfig(subDirectory: String, world: String) {
        val worldsDir = BloodmoonPlugin.instance.dataFolder.resolve(subDirectory)
        if (!worldsDir.exists()) {
            worldsDir.mkdir()
        }
        if (BloodmoonPlugin.instance.dataFolder.resolve("$subDirectory/$world.yml").exists()) return
        BloodmoonPlugin.instance.getResource("DefaultWorldConfig.yml").use {
            Files.copy(it!!, BloodmoonPlugin.instance.dataFolder.resolve("$subDirectory/$world.yml").toPath())
        }
    }

    override fun beforeReload(plugin: LibreforgePlugin) {
        // Check for worlds that don't have a config and create one.
        for (world in Bukkit.getWorlds()) {
            if (!isWorldEnabled(world.name)) continue
            createWorldConfig("worlds", world.name)
        }
    }

    /**
     * This method registers a world with the BloodmoonRegistry.
     * This should only need to be used for adding worlds at runtime.
     */
    fun registerWorld(id: String) {
        if (!isWorldEnabled(id)) return
        createWorldConfig("worlds", id)
        val file = BloodmoonPlugin.instance.dataFolder.resolve("worlds").walk().find { it.nameWithoutExtension == id }
        if (file == null) {
            // This should never happen, but just in case.
            BloodmoonPlugin.instance.logger.warning("World $id does not have a config file. Skipping...")
            return
        }
        val config = RegistrableConfig(file.readConfig(), file, id, this)
        acceptConfig(BloodmoonPlugin.instance, id, config.config)
    }

    override fun acceptConfig(
        plugin: LibreforgePlugin,
        id: String,
        config: Config
    ) {
        // Each config file is checked inside the "worlds" folder,
        val world =
            Bukkit.getWorld(id) ?: run {
                BloodmoonPlugin.instance.logger.warning("World $id does not exist. Skipping...")
                return
            }

        if (registry.find { it.world == world } != null) {
            BloodmoonPlugin.instance.logger.warning("World $id is already registered.")
            return
        }

        if (!isWorldEnabled(id)) {
            return
        }

        BloodmoonPlugin.instance.logger.info("Loading config for world $id")

        try {
            addToRegistry(BloodmoonWorld(world,config))
        } catch (e: Exception) {
            BloodmoonPlugin.instance.logger.severe("Failed to load config for world \"$id\": ${e.message}")
            if (BloodmoonPlugin.instance.configYml.getBool("debug")) e.printStackTrace()
            return
        }
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}
