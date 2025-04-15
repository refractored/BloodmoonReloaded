package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class BloodmoonVersionCommand {
    @CommandPermission("bloodmoon.command.version")
    @Description("Version info about the plugin.")
    @Command("bloodmoon version")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor
    ) {
        var info = BloodmoonPlugin.instance.langYml
            .getStringPrefixed("messages.version.success.version")
            .replace("%version%", BloodmoonPlugin.instance.pluginMeta.version)
            .replace("%author%", BloodmoonPlugin.instance.pluginMeta.authors.joinToString(", ")) + "\n"

        if (BloodmoonPlugin.instance.extensionLoader.loadedExtensions.isEmpty()) {
            info += BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.version.success.no-extensions")
        } else {
            for ((index, extension) in BloodmoonPlugin.instance.extensionLoader.loadedExtensions.withIndex()) {
                info += BloodmoonPlugin.instance.langYml
                    .getString("messages.version.success.extension")
                    .replace("%name%", extension.name)
                    .replace("%version%", extension.version)
                    .replace("%author%", extension.author)
                if (index != BloodmoonPlugin.instance.extensionLoader.loadedExtensions.size - 1) {
                    info += "\n"
                }
            }
        }

        actor.reply(info.miniToComponent())
    }
}
