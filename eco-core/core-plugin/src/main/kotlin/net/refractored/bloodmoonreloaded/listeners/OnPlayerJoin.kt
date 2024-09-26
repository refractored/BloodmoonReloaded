package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class OnPlayerJoin : Listener {
    @EventHandler
    fun execute(event: PlayerJoinEvent) {
        val bloodmoonDestination = BloodmoonRegistry.getWorld(event.player.world.name) ?: return

        if (bloodmoonDestination.status != BloodmoonWorld.BloodmoonStatus.ACTIVE) return

        if (!bloodmoonDestination.bossbarEnabled) return

        bloodmoonDestination.bossbar.addViewer(event.player)
    }
}
