package net.refractored.bloodmoonreloaded.listeners

import com.willfp.eco.util.toComponent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.util.MessageUtil.replace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnPlayerDeath : Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun execute(event: PlayerDeathEvent) {
        val world = BloodmoonRegistry.getWorld(event.player.world.name) ?: return

        if (world.clearInventory){
            event.player.inventory.clear()
            event.drops.clear()
        }

        if (world.clearEXP){
            event.droppedExp = 0
            event.player.exp = 0f
        }

        if (world.useCustomDeathMessage){
            event.deathMessage(
                world.customDeathMessage
                    .replace("%player%", event.player.name)
                    .replace("%player_displayname%", event.player.displayName())
                    .toComponent()
            )
        }
    }
}