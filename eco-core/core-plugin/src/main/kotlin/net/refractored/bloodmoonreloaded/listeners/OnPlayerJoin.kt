package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class OnPlayerJoin : Listener {
    @EventHandler
    fun execute(event: PlayerJoinEvent) {
        val bloodmoonDestination = BloodmoonRegistry.getWorld(event.player.world.name) ?: return

        if (bloodmoonDestination.active == null) return

        if (bloodmoonDestination.active != null) {
            if (!bloodmoonDestination.bossbarEnabled) return
            bloodmoonDestination.active!!.bossbar.addViewer(event.player)
        }
    }
}
