package net.refractored.bloodmoonreloaded.worlds

import net.kyori.adventure.bossbar.BossBar
import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent

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
        bloodmoonWorld.world.players.forEach {
            bossbar.addViewer(it)
        }
        bloodmoonWorld.savedBloodmoonRemainingMillis = length.toDouble()
    }
}
