package net.refractored.discord

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.extensions.Extension
import github.scarsz.discordsrv.DiscordSRV
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.discord.discord.DiscordRegistry
import net.refractored.discord.listeners.OnBloodmoonStart
import net.refractored.discord.listeners.OnBloodmoonStop
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Suppress("unused")
class DiscordExtension(
    plugin: EcoPlugin
) : Extension(plugin) {
    init {
        instance = this
    }

    lateinit var config: YamlConfiguration
        private set

    lateinit var discord: DiscordSRV

    override fun onEnable() {
        if (!File(dataFolder, CONFIG).exists()) {
            val destination = Path.of(dataFolder.absolutePath + "/$CONFIG")

            this.javaClass.getResourceAsStream("/$CONFIG")?.use { inputStream ->
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
            }!!
        }

        discord = (Bukkit.getPluginManager().getPlugin("DiscordSRV") ?: throw IllegalStateException("DiscordSRV is not loaded.")) as DiscordSRV

        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStart())
        BloodmoonPlugin.instance.eventManager.registerListener(OnBloodmoonStop())
    }

    override fun onAfterLoad() {
    }

    override fun onDisable() {
    }

    override fun onReload() {
        // No need to re-register listeners in OnBloodmoonStart, as all bloodmoons & tasks are stopped on reload.
        config = YamlConfiguration.loadConfiguration(dataFolder.resolve(CONFIG))
        DiscordRegistry.refreshConfigs()
    }

    companion object {
        /**
         * The extension's instance
         */
        @JvmStatic
        lateinit var instance: DiscordExtension
            private set

        private const val CONFIG: String = "discord.yml"
    }
}
