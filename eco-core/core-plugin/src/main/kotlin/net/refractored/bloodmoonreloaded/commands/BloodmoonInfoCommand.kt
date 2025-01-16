package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.bukkit.player

class BloodmoonInfoCommand {
    @CommandPermission("bloodmoon.command.info")
    @Description("Info about a bloodmoon.")
    @Command("bloodmoon info")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor,
        @Optional world: World = actor.player.world
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-bloodmoon-world")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.bloodmoon-info-active")
                    .replace("%world%", world.name)
                    .miniToComponent()
            )
            return
        }
        actor.reply(bloodmoonWorld.info)
    }
}
