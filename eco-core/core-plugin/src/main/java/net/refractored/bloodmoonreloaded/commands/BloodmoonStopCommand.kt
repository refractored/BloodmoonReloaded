package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
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

class BloodmoonStopCommand {
    @CommandPermission("bloodmoon.admin.bloodmoon.stop")
    @Description("Stops a bloodmoon.")
    @Command("bloodmoon stop")
    @Suppress("UNUSED")
    fun execute(
        actor: BukkitCommandActor,
        @Optional world: World = actor.requirePlayer().world ,
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-bloodmoon")
                    .replace("%world%", world.name)
                    .miniToComponent()
            )
        if (bloodmoonWorld.status == BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.deactivate.inactive")
                    .miniToComponent()
            )
        }
//        if (bloodmoonWorld is NoneBloodmoon && bloodmoonWorld.permanentBloodmoon) {
//            throw CommandErrorException(
//                BloodmoonPlugin.instance.langYml
//                    .getStringPrefixed("messages.deactivate.permanent")
//                    .miniToComponent()
//            )
//        }
        bloodmoonWorld.deactivate(StopCause.COMMAND)
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.deactivate.success")
                .replace("%world%", world.name)
                .miniToComponent()
        )
    }
}
