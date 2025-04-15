package net.refractored.hordes

import com.sk89q.worldguard.WorldGuard
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.hordes.commands.SpawnHordeCommand
import net.refractored.hordes.hordes.HordeRegistry
import net.refractored.hordes.listeners.OnBloodmoonStart
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import com.sk89q.worldguard.protection.flags.StateFlag
import java.nio.file.StandardCopyOption

@Suppress("unused")
class HordesExtension(
    plugin: EcoPlugin
) : Extension(plugin) {
    init {
        instance = this
    }

    var worldguard: WorldGuard? = null
        private set

    var hordesFlag: StateFlag? = null
        private set

    lateinit var hordeConfig: YamlConfiguration
        private set

    override fun onLoad() {
        // No need to load anything here
    }

    override fun onEnable() {
        if (!File(dataFolder, "hordes.yml").exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/hordes.yml")

            this.javaClass.getResourceAsStream("/hordes.yml")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }

        hordeConfig = YamlConfiguration.loadConfiguration(dataFolder.resolve("hordes.yml"))

        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStart())

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldguard = WorldGuard.getInstance()

            hordesFlag = StateFlag("hordes", true)

            worldguard!!.flagRegistry.register(hordesFlag!!)
        }

    }

    override fun onAfterLoad() {
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
