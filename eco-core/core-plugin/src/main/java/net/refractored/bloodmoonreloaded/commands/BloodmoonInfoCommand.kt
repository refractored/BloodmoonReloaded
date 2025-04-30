package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class BloodmoonInfoCommand {
    @CommandPermission("bloodmoon.command.info")
    @Description("Info about a bloodmoon.")
    @Command("bloodmoon info")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor,
        @Optional world: World = actor.requirePlayer().world,
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-bloodmoon")
                    .replace("%world%", world.name)
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.info.active")
                    .replace("%world%", world.name)
                    .miniToComponent()
            )
            return
        }
        actor.reply(bloodmoonWorld.activationMethod.getInfo())
    }
}
