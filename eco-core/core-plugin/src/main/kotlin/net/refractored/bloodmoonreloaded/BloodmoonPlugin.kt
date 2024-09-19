package net.refractored.bloodmoonreloaded

import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import net.refractored.bloodmoonreloaded.commands.BloodmoonStartCommand
import net.refractored.bloodmoonreloaded.commands.BloodmoonStopCommand
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorHandler
import net.refractored.bloodmoonreloaded.libreforge.IsBloodmoonActive
import net.refractored.bloodmoonreloaded.listeners.OnWorldLoad
import net.refractored.bloodmoonreloaded.listeners.OnWorldUnload
import net.refractored.bloodmoonreloaded.runnables.ExpireBloodmoons
import net.refractored.bloodmoonreloaded.runnables.UpdateSavedData
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import revxrsal.commands.bukkit.BukkitCommandHandler

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            BloodmoonRegistry,
        )

    override fun handleEnable() {
        instance = this

        handler = BukkitCommandHandler.create(this)

        handler.setExceptionHandler(CommandErrorHandler())

        handler.register(BloodmoonStartCommand())
        handler.register(BloodmoonStopCommand())

        Conditions.register(IsBloodmoonActive)

        registerGenericHolderProvider {
            BloodmoonRegistry.getActiveWorlds().map { SimpleProvidedHolder(it) }
        }
    }

    override fun handleAfterLoad() {
        // Registered after to prevent issues.
        eventManager.registerListener(OnWorldLoad())
        eventManager.registerListener(OnWorldUnload())

        UpdateSavedData.runTimer()
        ExpireBloodmoons.runTimer()
    }

    override fun handleReload() {
    }

    override fun handleDisable() {
        if (this::handler.isInitialized) {
            handler.unregisterAllCommands()
        }
        UpdateSavedData.stopTimer()
        ExpireBloodmoons.stopTimer()
    }

    companion object {
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
