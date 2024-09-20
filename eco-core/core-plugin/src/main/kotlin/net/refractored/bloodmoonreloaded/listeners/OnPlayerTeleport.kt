package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class OnPlayerTeleport : Listener {
    @EventHandler
    fun execute(event: PlayerTeleportEvent) {
        val origin = event.to.world
        val destination = event.from.world

        if (origin == destination) return

        val bloodmoonOrigin = BloodmoonRegistry.getWorld(event.to.world.name)
        val bloodmoonDestination = BloodmoonRegistry.getWorld(event.from.world.name)

        if (bloodmoonOrigin == null && bloodmoonDestination == null) return

        if (bloodmoonOrigin?.active == null && bloodmoonDestination?.active == null) return

        if (bloodmoonOrigin?.active != null) {
            bloodmoonOrigin.active!!.bossbar.removeViewer(event.player)
        }

        if (bloodmoonDestination?.active != null) {
            bloodmoonDestination.active!!.bossbar.addViewer(event.player)
        }
    }
}
