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
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getWorlds
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import org.bukkit.scheduler.BukkitRunnable
import revxrsal.commands.bukkit.BukkitCommandHandler

class BloodmoonPlugin : LibreforgePlugin() {
    lateinit var handler: BukkitCommandHandler

    override fun loadConfigCategories(): List<ConfigCategory> =
        listOf(
            BloodmoonRegistry
        )

    // Libreforge Plugin Load Order
    // onEnable (Extensions) -> OnEnable (Plugin) -> Reload (See Below)
    //
    // Reload:
    // handleReload -> createTasks -> onReload (Extensions)
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
        // Lamp does not allow (or just breaks) whenever commands are registered after brigadier.
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
        for (world in getWorlds()) {
            if (world.status != BloodmoonWorld.Status.ACTIVE) continue
            if (world.savedBloodmoonRemainingMillis <= 0) continue
            world.activate(world.savedBloodmoonRemainingMillis.toLong(), false)
        }
    }

    override fun handleDisable() {
        if (this::handler.isInitialized) {
            handler.unregisterAllCommands()
        }
    }

    override fun createTasks() {
        // All tasks are cancelled on reload, so we don't need to worry about having duplicate tasks.
        val periodicTasks =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getWorlds()) {
                        registeredWorld.runPeriodicTasks()
                    }
                }
            }
        scheduler.runAsyncTimer(periodicTasks, 1 , 20)

        // Checks if a bloodmoon should be activated or deactivated.
        val checkBloodmoons =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getWorlds()) {
                        if (registeredWorld.status != BloodmoonWorld.Status.ACTIVE
                            &&
                            (System.currentTimeMillis() >= registeredWorld.expiryTime && registeredWorld.expiryTime >= 0)) {
                            registeredWorld.deactivate(BloodmoonStopEvent.StopCause.TIMER)
                            return
                        }
                        // If either the bloodmoon shouldn't activate, or the status isn't active, return.
                        // (Written here cause my dyslexic-ass has messed with me trying to read this for some reason)
                        if (!registeredWorld.shouldActivate() || registeredWorld.status != BloodmoonWorld.Status.INACTIVE) continue
                        registeredWorld.activate()
                    }
                }
            }
        scheduler.runTimer(checkBloodmoons, 0, 2)
    }


    companion object {
        @JvmStatic
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
