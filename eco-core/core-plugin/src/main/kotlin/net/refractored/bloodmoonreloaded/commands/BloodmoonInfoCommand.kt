package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.DaysBloodmoon
import net.refractored.bloodmoonreloaded.types.NoneBloodmoon
import net.refractored.bloodmoonreloaded.types.TimedBloodmoon
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import net.refractored.bloodmoonreloaded.util.MessageUtil.replace
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.bukkit.player
import java.time.Duration

class BloodmoonInfoCommand {
    @CommandPermission("bloodmoon.command.info")
    @Description("Info about a bloodmoon")
    @Command("bloodmoon stop")
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
        when (bloodmoonWorld) {
            is TimedBloodmoon -> {
                val timeframe = Duration.ofMillis(bloodmoonWorld.millisUntilActivation - System.currentTimeMillis())
                actor.reply(
                    BloodmoonPlugin.instance.langYml
                        .getStringPrefixed("messages.bloodmoon-info-time")
                        .replace("%world%", world.name)
                        .replace("%status%", bloodmoonWorld.status.toString())
                        .replace("%hours%", timeframe.toHours().toString())
                        .replace("%minutes%", timeframe.toMinutesPart().toString())
                        .replace("%seconds%", timeframe.toSecondsPart().toString())
                        .miniToComponent()
                )
                return
            }
            is DaysBloodmoon -> {
                actor.reply(
                    BloodmoonPlugin.instance.langYml
                        .getStringPrefixed("messages.bloodmoon-info")
                        .replace("%world%", world.name)
                        .replace("%status%", bloodmoonWorld.status.toString())
                        .replace("%days%", bloodmoonWorld.dayCount.toString())
                        .miniToComponent()
                )
                return
            }
            is NoneBloodmoon -> {
                actor.reply(
                    BloodmoonPlugin.instance.langYml
                        .getStringPrefixed("messages.bloodmoon-info-none")
                        .replace("%world%", world.name)
                        .replace("%status%", bloodmoonWorld.status.toString())
                        .miniToComponent()
                )
                return
            }
        }
    }
}
