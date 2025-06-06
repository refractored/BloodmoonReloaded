package net.refractored.discord.listeners

import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.discord.DiscordExtension
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class OnBloodmoonStart : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun run(event: BloodmoonStartEvent) {
        val config = DiscordExtension.instance.configHandler.getSection(event.world) ?: return

        config.startChannels?.forEach { channel -> channel.sendMessage(config.configSection.getString("start-message.message") ?: return).queue() }
    }
}
