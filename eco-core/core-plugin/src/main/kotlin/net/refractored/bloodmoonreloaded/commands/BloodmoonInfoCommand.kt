package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.*
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
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
                        .getStringPrefixed("messages.bloodmoon-info-days")
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
            is ChanceBloodmoon -> {
                actor.reply(
                    BloodmoonPlugin.instance.langYml
                        .getStringPrefixed("messages.bloodmoon-info-chance")
                        .replace("%world%", world.name)
                        .replace("%status%", bloodmoonWorld.status.toString())
                        .replace("%chance%", (bloodmoonWorld.chance * 100).toString())
                        .miniToComponent()
                )
                return
            }
            is ChanceBloodmoon -> {
                actor.reply(
                    BloodmoonPlugin.instance.langYml
                        .getStringPrefixed("messages.bloodmoon-info-chance")
                        .replace("%world%", world.name)
                        .replace("%status%", bloodmoonWorld.status.toString())
                        .replace("%chance%", (bloodmoonWorld.chance * 100).toString())
                        .miniToComponent()
                )
                return
            }
        }
    }
}
