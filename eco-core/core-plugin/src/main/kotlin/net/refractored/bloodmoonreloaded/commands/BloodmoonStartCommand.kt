package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class BloodmoonStartCommand {
    @CommandPermission("bloodmoon.admin.bloodmoon.start")
    @Description("Starts a bloodmoon.")
    @Command("bloodmoon start")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor,
        @Optional world: World = actor.requirePlayer().world,
        ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-bloodmoon")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
                throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.activate.already-active")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.activate()
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.activate.success")
                .replace("%world%", world.name)
                .miniToComponent()
        )
    }
}
