package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class BloodmoonReloadCommand {
    @CommandPermission("bloodmoon.admin.reload")
    @Description("Reloads the plugin.")
    @Command("bloodmoon reload")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor,
        @Optional confirm: Boolean = false
    ) {
        if (!confirm && BloodmoonRegistry.getActiveWorlds().isNotEmpty()) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.confirm-reload")
                    .miniToComponent()
            )
        }
        BloodmoonPlugin.instance.reload()
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.reloaded")
                .miniToComponent()
        )
    }
}
