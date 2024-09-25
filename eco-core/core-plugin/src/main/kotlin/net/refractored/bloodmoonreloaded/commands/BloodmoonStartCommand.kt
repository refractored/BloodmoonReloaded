package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.bukkit.player

class BloodmoonStartCommand {
    @CommandPermission("bloodmoon.admin.bloodmoon.start")
    @Description("Starts a bloodmoon.")
    @Command("bloodmoon start")
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
        bloodmoonWorld.active?.let {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.bloodmoon-already-active")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.activate()
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-activated")
                .miniToComponent()
        )
    }
}
