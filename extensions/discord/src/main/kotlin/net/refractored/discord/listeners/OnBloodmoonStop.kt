package net.refractored.discord.listeners

import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent
import net.refractored.discord.discord.DiscordRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class OnBloodmoonStop : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun run(event: BloodmoonStopEvent) {
        val config = DiscordRegistry.getConfig(event.world) ?: return

        config.stopChannels?.forEach { channel -> channel.sendMessage(config.configSection.getString("stop-message.message") ?: return).queue() }
    }
}
