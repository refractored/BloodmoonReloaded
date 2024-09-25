package net.refractored.bloodmoonreloaded.worlds

import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.GameRule
import org.bukkit.scheduler.BukkitRunnable

/**
 * Represents an active bloodmoon.
 */
class ActiveBloodmoon(
    val bloodmoonWorld: BloodmoonWorld,
    /**
     * The initial length of the bloodmoon.
     */
    var length: Long = bloodmoonWorld.length
) {
    var fullTime: Long = bloodmoonWorld.world.fullTime

    var bossbar =
        BossBar.bossBar(
            bloodmoonWorld.config
                .getString("Bossbar.Title")
                .miniToComponent(),
            1.0f,
            bloodmoonWorld.bossbarColor,
            bloodmoonWorld.bossbarStyle
        )

    /**
     * The time the bloodmoon expires.
     */
    val expiryTime = System.currentTimeMillis() + length

    /**
     * @return The remaining bloodmoon time in milliseconds
     */
    val remainingTime: Long
        get() {
            val time = System.currentTimeMillis()
            if ((expiryTime - time) <= 0) {
                return 0
            }
            return expiryTime - time
        }

    init {

        if (bloodmoonWorld.setDaylightCycle) {
            bloodmoonWorld.revertDaylightCycle = true
            bloodmoonWorld.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }

        bloodmoonWorld.savedBloodmoonRemainingMillis = length.toDouble()
        // Init is weird and doesn't let you return, don't shun me for nesting :c
        if (!bloodmoonWorld.bossbarEnabled) {
            bloodmoonWorld.world.players.forEach {
                bossbar.addViewer(it)
            }
            if (bloodmoonWorld.createFog) {
                bossbar.addFlags(BossBar.Flag.CREATE_WORLD_FOG)
            }
            if (bloodmoonWorld.darkenScreen) {
                bossbar.addFlags(BossBar.Flag.DARKEN_SCREEN)
            }

            BloodmoonPlugin.instance.scheduler.runTimer(
                object : BukkitRunnable() {
                    override fun run() {
                        val progress =
                            if (bloodmoonWorld.isIncreasing) {
                                val elapsedTime = System.currentTimeMillis() - (expiryTime - length)
                                (elapsedTime.toDouble() / length.toDouble()).coerceIn(0.0, 1.0).toFloat()
                            } else {
                                val remainingTime = expiryTime - System.currentTimeMillis()
                                (remainingTime.toDouble() / length.toDouble()).coerceIn(0.0, 1.0).toFloat()
                            }
                        bossbar.progress(progress)
                        if (bloodmoonWorld.active == null) {
                            cancel()
                        }
                    }
                },
                1,
                1
            )
        }
    }
}
