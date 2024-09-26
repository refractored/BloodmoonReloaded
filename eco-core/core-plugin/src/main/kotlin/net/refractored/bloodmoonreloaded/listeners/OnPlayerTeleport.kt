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

        if (bloodmoonOrigin == null && bloodmoonDestination == null) return

        if (bloodmoonOrigin?.status != BloodmoonWorld.BloodmoonStatus.INACTIVE && bloodmoonDestination?.status != BloodmoonWorld.BloodmoonStatus.INACTIVE) return

        if (bloodmoonOrigin?.status != BloodmoonWorld.BloodmoonStatus.ACTIVE) {
            bloodmoonOrigin?.bossbar?.removeViewer(event.player)
        }

        if (bloodmoonDestination?.status != BloodmoonWorld.BloodmoonStatus.ACTIVE) {
            if (bloodmoonDestination?.bossbarEnabled == false) return
            bloodmoonDestination?.bossbar?.addViewer(event.player)
        }
    }
}
