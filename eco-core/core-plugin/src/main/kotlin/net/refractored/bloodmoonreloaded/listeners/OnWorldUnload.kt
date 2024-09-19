package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.worlds.BloodmoonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class OnWorldUnload : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun execute(event: WorldLoadEvent) {
        BloodmoonRegistry.unregisterWorld(event.world.name)
    }
}
