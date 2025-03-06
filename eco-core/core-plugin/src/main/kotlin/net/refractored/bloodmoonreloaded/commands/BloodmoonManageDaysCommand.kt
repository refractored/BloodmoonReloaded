  package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.DaysBloodmoon
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.bukkit.player

@Command("bloodmoon manage days")
class  BloodmoonManageDaysCommand {
    @CommandPermission("bloodmoon.admin.manage.days.set")
    @Description("Sets the day count for a bloodmoon.")
    @Subcommand("set")
    @Suppress("UNUSED")
    fun setDays(
        actor: BukkitCommandActor,
        @Optional world: World = actor.player.world,
        number: Int
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-bloodmoon-world")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.cant-modify-active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld !is DaysBloodmoon) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-days-bloodmoon")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.dayCount = number
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-manage-days-set")
                .replace("%world%", world.name)
                .replace("%days%", number.toString())
                .miniToComponent()
        )
    }

    @CommandPermission("bloodmoon.admin.manage.days.add")
    @Description("Adds to the day count for a bloodmoon.")
    @Subcommand("add")
    @Suppress("UNUSED")
    fun addDays(
        actor: BukkitCommandActor,
        @Optional world: World = actor.player.world,
        number: Int
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-bloodmoon-world")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.cant-modify-active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld !is DaysBloodmoon) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-days-bloodmoon")
                    .miniToComponent()
            )
        }
        val result = bloodmoonWorld.dayCount + number
        bloodmoonWorld.dayCount = result
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-manage-days-set")
                .replace("%world%", world.name)
                .replace("%days%", result.toString())
                .miniToComponent()
        )
    }

    @CommandPermission("bloodmoon.admin.manage.days.remove")
    @Description("Takes to the day count for a bloodmoon.")
    @Subcommand("remove")
    @Suppress("UNUSED")
    fun removeDays(
        actor: BukkitCommandActor,
        @Optional world: World = actor.player.world,
        number: Int
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-bloodmoon-world")
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.cant-modify-active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld !is DaysBloodmoon) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.not-a-days-bloodmoon")
                    .miniToComponent()
            )
        }
        val result = bloodmoonWorld.dayCount - number
        if (result < 0) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.bloodmoon-manage-days-set-negative")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.dayCount = result
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.bloodmoon-manage-days-set")
                .replace("%world%", world.name)
                .replace("%days%", result.toString())
                .miniToComponent()
        )
    }
}
