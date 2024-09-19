package net.refractored.bloodmoonreloaded.runnables

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.worlds.BloodmoonWorld
import org.bukkit.scheduler.BukkitTask

object UpdateSavedData : Runnable {
    private var scheduler: BukkitTask? = null

    override fun run() {
        for (registeredWorld in BloodmoonRegistry.getRegisteredWorlds()) {
            registeredWorld.active?.let { active ->
                registeredWorld.savedMillisUntilActivation = (active.expiryTime - System.currentTimeMillis()).toDouble()
            }
            if (registeredWorld.activationType == BloodmoonWorld.BloodmoonActivation.DAYS) {
                registeredWorld.savedDaysUntilActivation = registeredWorld.daysUntilActivation
            }
            if (registeredWorld.activationType == BloodmoonWorld.BloodmoonActivation.TIMED) {
                registeredWorld.savedMillisUntilActivation = registeredWorld.millisUntilActivation
            }
        }
    }

    fun isRunning(): Boolean = scheduler != null

    fun runTimer() {
        if (scheduler != null) {
            throw IllegalStateException("Timer is already running")
        }
        scheduler = BloodmoonPlugin.instance.scheduler.runTimer(this, 0, 20 * 10)
    }

    fun stopTimer() {
        scheduler?.cancel()
        scheduler = null
    }
}
