package net.refractored.customSpawning

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.customSpawning.config.SpawnConfigRegistry
import net.refractored.customSpawning.listeners.OnEntitySpawn
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Suppress("unused")
class CustomSpawningExtension(
    plugin: EcoPlugin,
) : Extension(plugin) {
    /**
     * The raw YamlConfiguration of the spawn config.
     */
    lateinit var spawnConfigYml: YamlConfiguration
        private set

    init {
        instance = this
    }

    override fun onEnable() {
    }

    override fun onAfterLoad() {
        if (!File(dataFolder, "mobs.yml").exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/mobs.yml")

            this.javaClass.getResourceAsStream("/mobs.yml")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            } ?: throw IllegalArgumentException("Resource not found.")
        }

        spawnConfigYml = YamlConfiguration.loadConfiguration(dataFolder.resolve("mobs.yml"))

        SpawnConfigRegistry.refreshSpawnConfigs()

        BloodmoonPlugin.instance.eventManager.registerListener(OnEntitySpawn())
    }

    override fun onDisable() {
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: CustomSpawningExtension
            private set
    }
}
