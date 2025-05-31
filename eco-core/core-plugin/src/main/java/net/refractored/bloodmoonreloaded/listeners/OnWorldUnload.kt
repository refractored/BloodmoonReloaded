package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldUnloadEvent

class OnWorldUnload : Listener {
    @EventHandler
    fun execute(event: WorldUnloadEvent) {
        BloodmoonRegistry.getWorld(event.world.name) ?: return
        BloodmoonRegistry.unregisterWorld(event.world.name)
    }
}
