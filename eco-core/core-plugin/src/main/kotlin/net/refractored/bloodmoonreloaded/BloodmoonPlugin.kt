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
import kotlin.random.Random

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            BloodmoonRegistry
        )

    override fun handleEnable() {
        instance = this

        handler = BukkitCommandHandler.create(this)

        handler.exceptionHandler = CommandErrorHandler()

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
        // Registered after to prevent issues with extensions.
        eventManager.registerListener(OnWorldLoad())
        eventManager.registerListener(OnWorldUnload())
        eventManager.registerListener(OnPlayerTeleport())
        eventManager.registerListener(OnPlayerJoin())
        eventManager.registerListener(OnPlayerSleep())
        eventManager.registerListener(OnPlayerRespawn())
        eventManager.registerListener(OnPlayerDeath())
    }

    private var registeredBrigadier = false

    override fun handleReload() {
        // Lamp does not allow (or just breaks) whenever commands are registered after bridgadier.
        // Since extensions also register their own commands, and we want brigadier support, we want to
        // register ALL commands before brigadier. Auxilor has this function ran after all extensions are loaded.
        if (!registeredBrigadier) {
            handler.registerBrigadier()
            registeredBrigadier = true
        }
        // I'm not really sure if this is necessary.
        for (activeWorld in getActiveWorlds()) {
            activeWorld.deactivate(BloodmoonStopEvent.StopCause.RELOAD, false)
        }
        // All tasks are cancelled on reload, so we don't need to worry about having duplicate tasks.
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
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getActiveWorlds()) {
                        if (System.currentTimeMillis() >= registeredWorld.expiryTime && registeredWorld.expiryTime >= 0) {
                            registeredWorld.deactivate(BloodmoonStopEvent.StopCause.TIMER)
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

        val periodicTasks =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getRegisteredWorlds()) {
                        registeredWorld.runPeriodicTasks()
                    }
                    scheduler.runLater(this, (Random.nextLong(75) + 250L))
                }
            }
        scheduler.runLater(periodicTasks, 60)

        val checkBloodmoons =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getRegisteredWorlds()) {
                        if (!registeredWorld.shouldActivate() || registeredWorld.status != BloodmoonWorld.Status.INACTIVE) continue
                        registeredWorld.activate()
                    }
                }
            }
        scheduler.runTimer(checkBloodmoons, 0, 2)
        for (world in getRegisteredWorlds()) {
            if (world.status != BloodmoonWorld.Status.ACTIVE) continue
            if (world.savedBloodmoonRemainingMillis < 0) continue
            world.activate(world.savedBloodmoonRemainingMillis.toLong(), false)
        }
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
