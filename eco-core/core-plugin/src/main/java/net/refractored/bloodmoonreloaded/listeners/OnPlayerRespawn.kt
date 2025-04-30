package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class OnPlayerRespawn : Listener {
    @EventHandler
    fun execute(event: PlayerRespawnEvent) {
        val origin = event.player.world
        val destination = event.respawnLocation.world

        if (origin == destination) return

        val bloodmoonOrigin = BloodmoonRegistry.getWorld(origin.name)
        val bloodmoonDestination = BloodmoonRegistry.getWorld(destination.name)

        // If no config exists for either world, return
        if (bloodmoonOrigin == null && bloodmoonDestination == null) return

        // If the bloodmoon is not active in either world, return
        if (bloodmoonOrigin?.status != BloodmoonWorld.Status.ACTIVE && bloodmoonDestination?.status != BloodmoonWorld.Status.ACTIVE) return

        if (bloodmoonOrigin?.status == BloodmoonWorld.Status.ACTIVE) {
            bloodmoonOrigin.bossbar?.removeViewer(event.player)
        }

        if (bloodmoonDestination?.status == BloodmoonWorld.Status.ACTIVE) {
            bloodmoonDestination.bossbar?.addViewer(event.player)
        }
    }
}
