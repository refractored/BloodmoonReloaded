package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStopEvent.StopCause
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.NoneBloodmoon
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
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
        @Optional world: World = actor.asPlayer()?.world ?:
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-player")
                    .miniToComponent()
            )
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-bloodmoon-world")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status == BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.bloodmoon-not-active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld is NoneBloodmoon && bloodmoonWorld.permanentBloodmoon) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.cannot-stop-permanent")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.deactivate(StopCause.COMMAND)
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-deactivated")
                .miniToComponent()
        )
    }
}
