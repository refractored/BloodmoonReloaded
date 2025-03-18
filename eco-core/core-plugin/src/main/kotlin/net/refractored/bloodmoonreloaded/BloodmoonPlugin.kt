package net.refractored.bloodmoonreloaded

import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import net.refractored.bloodmoonreloaded.Polymart.checkPolymartStatus
import net.refractored.bloodmoonreloaded.Polymart.verifyPurchase
import net.refractored.bloodmoonreloaded.commands.*
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.libreforge.IsBloodmoonActive
import net.refractored.bloodmoonreloaded.listeners.*
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getActiveWorlds
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getWorlds
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import net.refractored.bloodmoonreloaded.types.*
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.scheduler.BukkitRunnable
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

class BloodmoonPlugin : LibreforgePlugin() {
//    lateinit var handler: BukkitCommandHandler

    lateinit var lamp: Lamp<BukkitCommandActor>
        private set

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

        lamp = BukkitLamp.builder(this)
            .build()

//        handler = BukkitCommandHandler.create(this)

//        lamp.exceptionHandler = CommandErrorHandler()

        lamp.register(BloodmoonStartCommand())
        lamp.register(BloodmoonStopCommand())
        lamp.register(BloodmoonReloadCommand())
        lamp.register(BloodmoonInfoCommand())
        lamp.register(BloodmoonManageDaysCommand())

        TypeRegistry.registerType("chance", ChanceBloodmoon)
        TypeRegistry.registerType("days", DaysBloodmoon)
        TypeRegistry.registerType("mirror", MirrorBloodmoon)
        TypeRegistry.registerType("none", NoneBloodmoon)
        TypeRegistry.registerType("timed", TimedBloodmoon)

        Conditions.register(IsBloodmoonActive)

        registerGenericHolderProvider {
            getActiveWorlds().map { SimpleProvidedHolder(it) }
        }

        val polymart =
            object : BukkitRunnable() {
                override fun run() {
                    if (!checkPolymartStatus()) return
                    if (verifyPurchase()) {
                        logger.info("Thank you for purchasing the plugin! ^_^")
                        return
                    }
                    logger.warning("Please consider purchasing this plugin on Polymart! :)")
                    logger.warning("No functionality will be lost, but you might not receive support.")
                }
            }
        scheduler.runAsync(polymart)

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
//            handler.registerBrigadier()

            registeredBrigadier = true
        }
        // I'm not really sure if this is necessary.
        for (activeWorld in getActiveWorlds()) {
            activeWorld.deactivate(BloodmoonStopEvent.StopCause.RELOAD, false)
            activeWorld.saveBloodmoonTime()
        }
        for (world in getWorlds()) {
            if (world.status != BloodmoonWorld.Status.INACTIVE) continue
            if (world.savedBloodmoonRemainingMillis <= 0) continue
            world.activate(world.savedBloodmoonRemainingMillis, false)
        }
    }

    override fun handleDisable() {
//        if (this::handler.isInitialized) {
//            handler.unregisterAllCommands()
//        }

        for (activeWorld in getActiveWorlds()) {
            activeWorld.deactivate(BloodmoonStopEvent.StopCause.RESTART, false)
            activeWorld.saveBloodmoonTime()
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
        scheduler.runTimer(periodicTasks, 1 , 20)

        // Checks if a bloodmoon should be activated or deactivated.
        val checkBloodmoons =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getWorlds()) {
                        if (registeredWorld.status == BloodmoonWorld.Status.ACTIVE
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
        scheduler.runTimer(checkBloodmoons,40, 2)
    }


    companion object {
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
