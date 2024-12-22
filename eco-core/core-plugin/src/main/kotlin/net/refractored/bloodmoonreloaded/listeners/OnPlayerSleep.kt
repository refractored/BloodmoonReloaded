package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent

class OnPlayerSleep : Listener {
    @EventHandler
    fun execute(event: PlayerBedEnterEvent) {
        val bloodmoonWorld = BloodmoonRegistry.getWorld(event.player.world.name) ?: return

        if (bloodmoonWorld.status == BloodmoonWorld.BloodmoonStatus.INACTIVE) return

        if (!bloodmoonWorld.bedDisabled) return

        event.isCancelled = true
        event.player.sendMessage(bloodmoonWorld.bedDenyMessage.miniToComponent())
    }
}
