package net.refractored.hordes

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.extensions.BloodmoonSectionLoader
import net.refractored.bloodmoonreloaded.extensions.ConfigSectionLoader
import net.refractored.hordes.commands.SpawnHordeCommand
import net.refractored.hordes.hordes.HordeConfig
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

//    var worldguard: WorldGuard? = null
//        private set
//
//    var hordesFlag: StateFlag? = null
//        private set

    lateinit var config: YamlConfiguration
        private set

    lateinit var configHandler: ConfigSectionLoader<HordeConfig>

    override fun onEnable() {
        if (!File(dataFolder, CONFIG).exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/$CONFIG")

            this.javaClass.getResourceAsStream("/$CONFIG")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }
        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStart())
    }

    override fun onAfterLoad() {
        BloodmoonPlugin.instance.lamp.register(SpawnHordeCommand())
    }

    override fun onDisable() {
    }

    override fun onReload() {
        // No need to re-register listeners in OnBloodmoonStart, as all bloodmoons & tasks are stopped on reload.
        config = YamlConfiguration.loadConfiguration(dataFolder.resolve(CONFIG))
        configHandler = BloodmoonSectionLoader(config) { HordeConfig(it) }
        configHandler.refreshConfigs()
    }

    companion object {
        /**
         * The extension's instance
         */
        lateinit var instance: HordesExtension
            private set

        private const val CONFIG: String = "hordes.yml"
    }
}
