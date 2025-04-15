package net.refractored.hordes.util

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import net.refractored.hordes.HordesExtension
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player

object EligibleUtil {
    fun World.getEligiblePlayers(): List<Player> = this.players.filter {
        it.gameMode == GameMode.SURVIVAL && !it.isVanished() && !it.regionValid()
    }

    private fun Player.isVanished(): Boolean = this.getMetadata("vanished").any { it.asBoolean() }

    private fun Player.regionValid(): Boolean {
        HordesExtension.instance.worldguard ?: return false
        val player = WorldGuardPlugin.inst().wrapPlayer(this)
        val status = HordesExtension.instance.worldguard?.platform?.regionContainer?.createQuery()?.queryState(
            player.location,
            player,
            HordesExtension.instance.hordesFlag
        ) ?: return false
        return status == StateFlag.State.ALLOW
    }
}
