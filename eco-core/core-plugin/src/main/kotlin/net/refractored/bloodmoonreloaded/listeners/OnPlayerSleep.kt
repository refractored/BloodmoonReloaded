package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent

class OnPlayerSleep : Listener {
    @EventHandler
    fun execute(event: PlayerBedEnterEvent) {
        val bloodmoonWorld = BloodmoonRegistry.getWorld(event.player.world.name) ?: return

        // It'd make sense to deny beds during the transition as well.
        if (bloodmoonWorld.status == BloodmoonWorld.Status.INACTIVE) return

        if (!bloodmoonWorld.config.getBool("while-active.beds.deny-sleep")) return

        event.isCancelled = true

        if (bloodmoonWorld.config.getBool("messages.bed-deny-enabled")) {
            event.player.sendMessage(bloodmoonWorld.config.getString("messages.bed-deny-message").miniToComponent())
        }
    }
}
