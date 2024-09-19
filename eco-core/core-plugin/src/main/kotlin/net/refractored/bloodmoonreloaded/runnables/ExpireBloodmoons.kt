package net.refractored.bloodmoonreloaded.runnables

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import org.bukkit.scheduler.BukkitTask

object ExpireBloodmoons : Runnable {
    private var scheduler: BukkitTask? = null

    override fun run() {
        for (registeredWorld in BloodmoonRegistry.getActiveWorlds()) {
            registeredWorld.active?.let { active ->
                if (System.currentTimeMillis() >= active.expiryTime) {
                    registeredWorld.deactivate()
                }
            }
        }
    }

    fun isRunning(): Boolean = scheduler != null

    fun runTimer() {
        if (scheduler != null) {
            throw IllegalStateException("Timer is already running")
        }
        scheduler = BloodmoonPlugin.instance.scheduler.runTimer(this, 0, 15)
    }

    fun stopTimer() {
        scheduler?.cancel()
        scheduler = null
    }
}
