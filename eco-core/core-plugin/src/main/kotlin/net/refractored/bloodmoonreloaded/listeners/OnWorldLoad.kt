package net.refractored.bloodmoonreloaded.listeners

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class OnWorldLoad : Listener {
    @EventHandler
    fun execute(event: WorldLoadEvent) {
        BloodmoonPlugin.instance.loadConfigCategories()
    }
}
