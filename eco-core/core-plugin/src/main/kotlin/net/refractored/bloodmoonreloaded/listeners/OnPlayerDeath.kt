package net.refractored.bloodmoonreloaded.listeners

import com.willfp.eco.util.toComponent
import net.refractored.bloodmoonreloaded.messages.Messages.replace
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnPlayerDeath : Listener {
    // Priority is set to lowest to be the first to modify item drops.
    @EventHandler(priority = EventPriority.LOW)
    fun execute(event: PlayerDeathEvent) {
        val world = BloodmoonRegistry.getWorld(event.player.world.name) ?: return
        if (world.status != BloodmoonWorld.Status.ACTIVE) return

        if (world.config.getBool("while-active.on-player-death.clear-inventory")) {
            event.player.inventory.clear()
            event.drops.clear()
        }

        if (world.config.getBool("while-active.on-player-death.clear-exp")) {
            event.setShouldDropExperience(false)
            event.droppedExp = 0
            event.player.exp = 0f
        }

        if (world.config.getBool("while-active.on-player-death.custom-death-message")) {
            event.deathMessage(
                world.config.getString("messages.death-message")
                    .replace("%player%", event.player.name)
                    .replace("%player_displayname%", event.player.displayName())
                    .toComponent()
            )
        }
    }
}
