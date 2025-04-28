package net.refractored.bloodmoonreloaded

import com.willfp.eco.core.extensions.Extension
import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import net.refractored.bloodmoonreloaded.Polymart.checkPolymartStatus
import net.refractored.bloodmoonreloaded.Polymart.setPurchaseBstats
import net.refractored.bloodmoonreloaded.Polymart.verifyPurchase
import net.refractored.bloodmoonreloaded.commands.*
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorHandler
import net.refractored.bloodmoonreloaded.libreforge.IsBloodmoonActive
import net.refractored.bloodmoonreloaded.listeners.*
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getActiveWorlds
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry.getWorlds
import net.refractored.bloodmoonreloaded.registry.TypeRegistry
import net.refractored.bloodmoonreloaded.types.*
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

class BloodmoonPlugin : LibreforgePlugin() {

    init {
        instance = this
    }

    lateinit var lamp: Lamp<BukkitCommandActor>
        private set

    lateinit var metrics: Metrics
        private set

    override fun loadConfigCategories(): List<ConfigCategory> = listOf(
        BloodmoonRegistry
    )

    // Libreforge Plugin Load Order
    // onEnable (Extensions) -> OnEnable (Plugin) -> Reload (See Below)
    //
    // Reload:
    // handleReload -> createTasks -> onReload (Extensions)
    override fun handleEnable() {
        lamp = BukkitLamp.builder(this)
            .exceptionHandler(CommandErrorHandler())
            .build()

        lamp.register(BloodmoonStartCommand())
        lamp.register(BloodmoonStopCommand())
        lamp.register(BloodmoonVersionCommand())
        lamp.register(BloodmoonReloadCommand())
        lamp.register(BloodmoonInfoCommand())
        lamp.register(BloodmoonManageDaysCommand())

        TypeRegistry.registerType("chance") { world, config -> ChanceBloodmoon(world, config) }
        TypeRegistry.registerType("days") { world, config -> DaysBloodmoon(world, config) }
        TypeRegistry.registerType("mirror") { world, config -> MirrorBloodmoon(world, config) }
        TypeRegistry.registerType("none") { world, config -> NoneBloodmoon(world, config) }
        TypeRegistry.registerType("timed") { world, config -> TimedBloodmoon(world, config) }

        metrics = Metrics(this, 24355)

        metrics.addCustomChart(
            AdvancedPie("extensions") {
                val values = HashMap<String, Int>()

                for (extension in extensionLoader.loadedExtensions) {
                    values[extension.name] = 1
                }

                if (values.isEmpty()) {
                    values["None"] = 1
                }

                values
            }
        )

        Conditions.register(IsBloodmoonActive)

        registerGenericHolderProvider {
            getActiveWorlds().map { SimpleProvidedHolder(it) }
        }

        // For some reason this isn't called by eco?
        afterLoad()
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

    override fun handleReload() {
        // I'm not really sure if this is necessary.
        // Eco might already cancel all bloodmoons on reload.
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
        if (this::lamp.isInitialized) {
            lamp.unregisterAllCommands()
        }
        for (activeWorld in getActiveWorlds()) {
            activeWorld.deactivate(BloodmoonStopEvent.StopCause.RESTART, false)
            activeWorld.saveBloodmoonTime()
        }
    }

    private var polymarted = false

    override fun createTasks() {
        if (!polymarted) {
            val polymart =
                object : BukkitRunnable() {
                    override fun run() {
                        if (!checkPolymartStatus()) return
                        if (verifyPurchase()) {
                            if (configYml.getBool("disable-purchase-message")) return
                            logger.info("Thank you for purchasing the plugin! ^_^")
                            setPurchaseBstats(true)
                            return
                        }
                        logger.warning("Please consider purchasing this plugin on Polymart! :)")
                        logger.warning("No functionality will be lost, but you might not receive support.")
                        setPurchaseBstats(false)
                    }
                }
            polymarted = true
            scheduler.runAsync(polymart)
        }
        // All tasks are cancelled on reload, so we don't need to worry about having duplicate tasks.
        val periodicTasks =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getWorlds()) {
                        registeredWorld.runPeriodicTasks()
                    }
                }
            }
        scheduler.runTimer(periodicTasks, 1, 20)

        // Checks if a bloodmoon should be activated or deactivated.
        val checkBloodmoons =
            object : BukkitRunnable() {
                override fun run() {
                    for (registeredWorld in getWorlds()) {
                        if (registeredWorld.status == BloodmoonWorld.Status.ACTIVE &&
                            (System.currentTimeMillis() >= registeredWorld.expiryTime && registeredWorld.expiryTime >= 0)
                        ) {
                            registeredWorld.deactivate(BloodmoonStopEvent.StopCause.TIMER)
                            return
                        }
                        if (registeredWorld.config.getBool("require-players-server") && Bukkit.getOnlinePlayers().count() < 1) {
                            continue
                        }
                        if (registeredWorld.config.getBool("require-players-world") && registeredWorld.world.playerCount < 1) {
                            continue
                        }
                        // If either the bloodmoon shouldn't activate, or the status isn't active, return.
                        // (Written here cause my dyslexic-ass has messed with me trying to read this for some reason)
                        if (!registeredWorld.shouldActivate() || registeredWorld.status != BloodmoonWorld.Status.INACTIVE) continue
                        registeredWorld.activate()
                    }
                }
            }
        scheduler.runTimer(checkBloodmoons, 40, 2)
    }

    companion object {
        /**
         * Logs info for an extension, backed by its extension name.
         * Helpful for debugging issues.
         */
        fun Extension.logInfo(msg: String) {
           this.logger.info("[${this.name}] " + msg)
        }

        /**
         * Logs a warning for an extension, backed by its extension name.
         * Helpful for debugging issues.
         */
        fun Extension.logWarn(msg: String) {
            this.logger.warning("[${this.name}] " + msg)
        }

        /**
         * Logs a severe warning for an extension, backed by its extension name.
         * Helpful for debugging issues.
         */
        fun Extension.logSevere(msg: String) {
            this.logger.severe("[${this.name}] " + msg)
        }


        @JvmStatic
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
