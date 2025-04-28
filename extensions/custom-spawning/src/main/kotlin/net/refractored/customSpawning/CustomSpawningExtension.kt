package net.refractored.customSpawning

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.extensions.BloodmoonSectionLoader
import net.refractored.bloodmoonreloaded.extensions.ConfigSectionLoader
import net.refractored.customSpawning.config.SpawnConfig
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
    lateinit var config: YamlConfiguration
        private set

    lateinit var configHandler: ConfigSectionLoader<SpawnConfig>

    init {
        instance = this
    }

    override fun onEnable() {
        TODO("Not yet implemented")
    }

    override fun onAfterLoad() {
        if (!File(dataFolder, CONFIG).exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/$CONFIG")

            this.javaClass.getResourceAsStream("/$CONFIG")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            } ?: throw IllegalArgumentException("Resource not found.")
        }
        BloodmoonPlugin.instance.eventManager.registerListener(OnEntitySpawn())
    }

    override fun onDisable() {
    }

    override fun onReload() {
        config = YamlConfiguration.loadConfiguration(dataFolder.resolve(CONFIG))
        configHandler = BloodmoonSectionLoader(config, this) { SpawnConfig(it) }
        configHandler.refreshConfigs()
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: CustomSpawningExtension
            private set

        private const val CONFIG: String = "mobs.yml"
    }
}
