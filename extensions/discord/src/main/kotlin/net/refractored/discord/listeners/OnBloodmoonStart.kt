package net.refractored.discord.listeners

import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld.Status
import net.refractored.discord.discord.DiscordRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class OnBloodmoonStart : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun run(event: BloodmoonStartEvent) {
        val bloodmoonWorld = BloodmoonRegistry.getWorld(event.world.name) ?: return

        if (bloodmoonWorld.status != Status.ACTIVE) return

        val config = DiscordRegistry.getConfig(event.world) ?: return

        config.startChannels?.forEach { channel -> channel.sendMessage(config.configSection.getString("start-message.message") ?: return).queue() }
    }
}
