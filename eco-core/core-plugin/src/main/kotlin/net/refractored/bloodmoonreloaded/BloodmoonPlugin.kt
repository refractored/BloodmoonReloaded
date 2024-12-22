package net.refractored.bloodmoonreloaded

import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import net.refractored.bloodmoonreloaded.commands.*
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorHandler
import net.refractored.bloodmoonreloaded.libreforge.IsBloodmoonActive
import net.refractored.bloodmoonreloaded.listeners.*
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getActiveWorlds
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getRegisteredWorlds
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import org.bukkit.scheduler.BukkitRunnable
import revxrsal.commands.bukkit.BukkitCommandHandler

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            BloodmoonRegistry
        )

    override fun handleEnable() {
        instance = this

        handler = BukkitCommandHandler.create(this)

        handler.setExceptionHandler(CommandErrorHandler())

        handler.register(BloodmoonStartCommand())
        handler.register(BloodmoonStopCommand())
        handler.register(BloodmoonReloadCommand())
        handler.register(BloodmoonInfoCommand())

        Conditions.register(IsBloodmoonActive)

        registerGenericHolderProvider {
            getActiveWorlds().map { SimpleProvidedHolder(it) }
        }
    }

    override fun handleAfterLoad() {
        // Registered after to prevent issues. I should've probably mentioned what issues, but I forgot ngl.
        eventManager.registerListener(OnWorldLoad())
        eventManager.registerListener(OnWorldUnload())
        eventManager.registerListener(OnPlayerTeleport())
        eventManager.registerListener(OnPlayerJoin())
        eventManager.registerListener(OnPlayerSleep())
        eventManager.registerListener(OnPlayerRespawn())
    }

    private var registeredBrigadier = false

    override fun handleReload() {
        // stupid workaround to register commands.
        if (!registeredBrigadier) {
            handler.registerBrigadier()
            registeredBrigadier = true
        }
        for (activeWorld in getActiveWorlds()) {
            activeWorld.deactivate(BloodmoonStopEvent.StopCause.RELOAD, false)
        }
        // All tasks are cancelled on reload.
        val updateSavedData =
            object : Runnable {
                override fun run() {
                    for (registeredWorld in getActiveWorlds()) {
                        registeredWorld.savedBloodmoonRemainingMillis = (registeredWorld.expiryTime - System.currentTimeMillis()).toDouble()
                        return
                    }
                }
            }
        scheduler.runTimer(updateSavedData, 1, 20)

        val updateBloodmoons =
            object : Runnable {
                override fun run() {
                    for (registeredWorld in getActiveWorlds()) {
                        if (System.currentTimeMillis() >= registeredWorld.expiryTime) {
                            registeredWorld.deactivate()
                            return
                        }
                        if (registeredWorld.setThunder) {
                            registeredWorld.world.setStorm(true)
                        }
                        registeredWorld.world.fullTime = registeredWorld.fullTime
                    }
                }
            }
        scheduler.runTimer(updateBloodmoons, 1, 16)

        val checkBloodmoons =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getRegisteredWorlds()) {
                        if (!registeredWorld.shouldActivate() || registeredWorld.status != BloodmoonWorld.BloodmoonStatus.INACTIVE) continue
                        registeredWorld.activate()
                    }
                }
            }
        scheduler.runTimer(checkBloodmoons, 0, 2)
    }

    override fun handleDisable() {
        if (this::handler.isInitialized) {
            handler.unregisterAllCommands()
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
