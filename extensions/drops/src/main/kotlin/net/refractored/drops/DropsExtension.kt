package net.refractored.drops

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.drops.drops.DropsRegistry
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

    lateinit var dropsConfig: YamlConfiguration
        private set

    var hordes: HordesExtension? = null

    override fun onEnable() {
    }

    override fun onAfterLoad() {
        if (!File(dataFolder, "drops.yml").exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/drops.yml")

            this.javaClass.getResourceAsStream("/drops.yml")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }

        dropsConfig = YamlConfiguration.loadConfiguration(dataFolder.resolve("drops.yml"))

        DropsRegistry.refreshConfigs()

        plugin.extensionLoader.loadedExtensions.find { it.name == "Hordes" }?.let {
            hordes = it as HordesExtension
            logger.info("Drops extension successfully hooked into Hordes.")
        }

        BloodmoonPlugin.instance.eventManager.registerListener(OnEntityDeath())
//        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStart())

//        BloodmoonPlugin.instance.lamp.register(SpawnHordeCommand())
    }

    override fun onDisable() {
    }

    override fun onReload() {
        // No need to re-register listeners in OnBloodmoonStart, as all bloodmoons & tasks are stopped on reload.
        dropsConfig = YamlConfiguration.loadConfiguration(dataFolder.resolve("drops.yml"))
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: DropsExtension
            private set
    }
}
