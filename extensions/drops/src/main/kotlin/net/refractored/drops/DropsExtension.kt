package net.refractored.drops

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.extensions.BloodmoonSectionLoader
import net.refractored.bloodmoonreloaded.extensions.ConfigSectionLoader
import net.refractored.drops.drops.DropsConfig
import net.refractored.drops.listeners.OnEntityDeath
import net.refractored.hordes.HordesExtension
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Suppress("unused")
class DropsExtension(
    plugin: EcoPlugin
) : Extension(plugin) {
    init {
        instance = this
    }

    lateinit var config: YamlConfiguration
        private set

    lateinit var configHandler: ConfigSectionLoader<DropsConfig>

    var hordes: HordesExtension? = null

    override fun onEnable() {
        if (!File(dataFolder, CONFIG).exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/$CONFIG")

            this.javaClass.getResourceAsStream("/$CONFIG")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }

        plugin.extensionLoader.loadedExtensions.find { it.name == "Hordes" }?.let {
            hordes = it as HordesExtension
            logger.info("Drops extension successfully hooked into Hordes.")
        }

        BloodmoonPlugin.instance.eventManager.registerListener(OnEntityDeath())
    }

    override fun onAfterLoad() {
    }

    override fun onDisable() {
    }

    override fun onReload() {
        config = YamlConfiguration.loadConfiguration(dataFolder.resolve(CONFIG))
        configHandler = BloodmoonSectionLoader(config) { DropsConfig(it) }
        configHandler.refreshConfigs()
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: DropsExtension
            private set

        private const val CONFIG: String = "drops.yml"
    }
}
