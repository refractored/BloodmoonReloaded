package net.refractored.hordes

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.hordes.commands.SpawnHordeCommand
import net.refractored.hordes.hordes.HordeRegistry
import net.refractored.hordes.listeners.OnBloodmoonStart
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Suppress("unused")
class HordesExtension(
    plugin: EcoPlugin
) : Extension(plugin) {
    init {
        instance = this
    }

    lateinit var hordeConfig: YamlConfiguration
        private set

    override fun onEnable() {
    }

    override fun onAfterLoad() {
        if (!File(dataFolder, "hordes.yml").exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/hordes.yml")

            this.javaClass.getResourceAsStream("/hordes.yml")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }

        hordeConfig = YamlConfiguration.loadConfiguration(dataFolder.resolve("hordes.yml"))

        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStart())

        BloodmoonPlugin.instance.lamp.register(SpawnHordeCommand())
    }

    override fun onDisable() {
    }

    override fun onReload() {
        // No need to re-register listeners in OnBloodmoonStart, as all bloodmoons & tasks are stopped on reload.
        hordeConfig = YamlConfiguration.loadConfiguration(dataFolder.resolve("hordes.yml"))

        HordeRegistry.refreshHordeConfigs()
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: HordesExtension
            private set
    }
}
