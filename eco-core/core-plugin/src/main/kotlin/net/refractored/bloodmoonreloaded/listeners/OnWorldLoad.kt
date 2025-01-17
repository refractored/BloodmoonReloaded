package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class OnWorldLoad : Listener {
    @EventHandler
    fun execute(event: WorldLoadEvent) {
        if (!BloodmoonRegistry.isWorldEnabled(event.world.name)) return
        // TODO: HORRIBLE IDEA, This will cancel ALL bloodmoons on world load
        BloodmoonPlugin.instance.loadConfigCategories()
    }
}
