package net.refractored.hordes.util

import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player

object EligibleUtil {
    fun World.getEligiblePlayers(): List<Player> =
        this.players.filter {
            it.gameMode == GameMode.SURVIVAL && !it.isVanished()
        }

    private fun Player.isVanished(): Boolean {
        for (meta in this.getMetadata("vanished")) {
            if (meta.asBoolean()) return true
        }
        return false
    }
}