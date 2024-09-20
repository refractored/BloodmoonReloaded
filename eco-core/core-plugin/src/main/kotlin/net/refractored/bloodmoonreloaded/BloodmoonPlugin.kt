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
import net.refractored.bloodmoonreloaded.listeners.OnPlayerTeleport
import net.refractored.bloodmoonreloaded.listeners.OnWorldLoad
import net.refractored.bloodmoonreloaded.listeners.OnWorldUnload
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry.getRegisteredWorlds
import net.refractored.bloodmoonreloaded.worlds.BloodmoonWorld
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
        eventManager.registerListener(OnPlayerTeleport())
    }

    override fun handleReload() {
        val updateSavedData =
            object : Runnable {
                override fun run() {
                    for (registeredWorld in getRegisteredWorlds()) {
                        registeredWorld.active?.let { active ->
                            registeredWorld.savedBloodmoonRemainingMillis = (active.expiryTime - System.currentTimeMillis()).toDouble()
                            return
                        }
                        if (registeredWorld.activationType == BloodmoonWorld.BloodmoonActivation.DAYS) {
                            registeredWorld.savedDaysUntilActivation = registeredWorld.daysUntilActivation
                            return
                        }
                        if (registeredWorld.activationType == BloodmoonWorld.BloodmoonActivation.TIMED) {
                            registeredWorld.savedMillisUntilActivation = registeredWorld.millisUntilActivation
                        }
                    }
                }
            }
        scheduler.runTimer(updateSavedData, 1, 20)

        val updateBloodmoons =
            object : Runnable {
                override fun run() {
                    for (registeredWorld in BloodmoonRegistry.getActiveWorlds()) {
                        val active = registeredWorld.active ?: continue
                        if (System.currentTimeMillis() >= active.expiryTime) {
                            registeredWorld.deactivate()
                            return
                        }
                        active.bossbar.progress(
                            (System.currentTimeMillis() / active.expiryTime.toDouble()).toFloat().coerceIn(
                                0.0F,
                                1.0F,
                            ),
                        )
                        registeredWorld.world.setStorm(true)
                        registeredWorld.world.fullTime = registeredWorld.active?.fullTime!!
                    }
                }
            }
        scheduler.runTimer(updateBloodmoons, 1, 15)

        val dayChecker =
            object : Runnable {
                override fun run() {
                    for (registeredWorld in getRegisteredWorlds()) {
                        if (registeredWorld.activationType != BloodmoonWorld.BloodmoonActivation.DAYS) return
                        if (registeredWorld.world.isDayTime) {
                            if (registeredWorld.active != null) {
                                registeredWorld.deactivate()
                                continue
                            }
                            if (!registeredWorld.lastDaytimeCheck) {
                                registeredWorld.daysUntilActivation += 1
                                registeredWorld.lastDaytimeCheck = true
                                continue
                            }
                            continue
                        }
                        registeredWorld.lastDaytimeCheck = false
                        if (registeredWorld.daysUntilActivation >= registeredWorld.activationDays && !registeredWorld.activating) {
                            registeredWorld.activate()
                        }
                    }
                }
            }
        scheduler.runTimer(dayChecker, 1, 5)
    }

    override fun handleDisable() {
        if (this::handler.isInitialized) {
            handler.unregisterAllCommands()
        }
    }

    companion object {
        lateinit var instance: BloodmoonPlugin
            private set
    }
}
