package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class OnPlayerTeleport : Listener {
    @EventHandler
    fun execute(event: PlayerTeleportEvent) {
        val origin = event.from.world
        val destination = event.to.world

        if (origin == destination) return

        val bloodmoonOrigin = BloodmoonRegistry.getWorld(origin.name)
        val bloodmoonDestination = BloodmoonRegistry.getWorld(destination.name)

        // If no config exists for either world, return
        if (bloodmoonOrigin == null && bloodmoonDestination == null) return

        // If the bloodmoon is not active in either world, return
        if (bloodmoonOrigin?.status != BloodmoonWorld.BloodmoonStatus.ACTIVE && bloodmoonDestination?.status != BloodmoonWorld.BloodmoonStatus.ACTIVE) return

        if (bloodmoonOrigin?.status == BloodmoonWorld.BloodmoonStatus.ACTIVE) {
            bloodmoonOrigin.bossbar.removeViewer(event.player)
        }

        if (bloodmoonDestination?.status == BloodmoonWorld.BloodmoonStatus.ACTIVE) {
            if (!bloodmoonDestination.bossbarEnabled) return
            bloodmoonDestination.bossbar.addViewer(event.player)
        }
    }
}
