package net.refractored.bloodmoonreloaded.worlds

import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.GameRule

/**
 * Represents an active bloodmoon.
 */
class ActiveBloodmoon(
    val bloodmoonWorld: BloodmoonWorld,
    /**
     * The initial length of the bloodmoon.
     */
    var length: Long = bloodmoonWorld.length,
) {
    var fullTime: Long = bloodmoonWorld.world.fullTime

    var bossbar =
        BossBar.bossBar(
            BloodmoonPlugin.instance.langYml
                .getString("bossbar-title")
                .miniToComponent(),
            1.0f,
            bloodmoonWorld.bossbarColor,
            bloodmoonWorld.bossbarStyle,
        )

    /**
     * The time the bloodmoon expires.
     */
    val expiryTime = System.currentTimeMillis() + length

    init {

        if (bloodmoonWorld.setDaylightCycle) {
            bloodmoonWorld.revertDaylightCycle = true
            bloodmoonWorld.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        }

        bloodmoonWorld.savedBloodmoonRemainingMillis = length.toDouble()
        if (bloodmoonWorld.bossbarEnabled) {
            bloodmoonWorld.world.players.forEach {
                bossbar.addViewer(it)
            }
            if (bloodmoonWorld.createFog) {
                bossbar.addFlags(BossBar.Flag.CREATE_WORLD_FOG)
            }
            if (bloodmoonWorld.darkenScreen) {
                bossbar.addFlags(BossBar.Flag.DARKEN_SCREEN)
            }
        }
    }
}
