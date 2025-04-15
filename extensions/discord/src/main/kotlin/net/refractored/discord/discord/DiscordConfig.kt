package net.refractored.discord.discord

import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.refractored.discord.DiscordExtension
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

data class DiscordConfig(
    val configSection: ConfigurationSection
) {
    val worlds: List<World> = configSection.getStringList("worlds").mapNotNull { Bukkit.getWorld(it) }

    val startChannels: List<TextChannel>? =
        if (configSection.getBoolean("start-message.enabled")) {
            configSection.getStringList("start-message.channels").mapNotNull { DiscordExtension.instance.discord.mainGuild.getTextChannelById(it) }
        } else {
            null
        }

    val stopChannels: List<TextChannel>? =
        if (configSection.getBoolean("stop-message.enabled")) {
            configSection.getStringList("stop-message.channels").mapNotNull { DiscordExtension.instance.discord.mainGuild.getTextChannelById(it) }
        } else {
            null
        }

    init {
        if (worlds.isEmpty()) {
            throw IllegalArgumentException("No valid worlds found")
        }
    }
}
