package net.refractored.bloodmoonreloaded.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.activation.DaysActivation
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.World
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Range
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("bloodmoon manage days")
class BloodmoonManageDaysCommand {
    @CommandPermission("bloodmoon.admin.manage.days.set")
    @Description("Sets the day count for a bloodmoon.")
    @Subcommand("set")
    @Suppress("UNUSED")
    fun setDays(
        actor: BukkitCommandActor,
        world: World,
        @Range(min = 0.0) number: Int
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
                    .getStringPrefixed("messages.manage-days.active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld.activationMethod !is DaysActivation) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.manage-days.not-days")
                    .miniToComponent()
            )
        }
        bloodmoonWorld.activationMethod.dayCount = number
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.manage-days.success")
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
        world: World,
        @Range(min = 1.0) number: Int
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
                    .getStringPrefixed("messages.manage-days.active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld.activationMethod !is DaysActivation) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.manage-days.not-days")
                    .miniToComponent()
            )
        }
        val result = bloodmoonWorld.activationMethod.dayCount + number
        bloodmoonWorld.activationMethod.dayCount = result
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.manage-days.success")
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
        world: World,
        @Range(min = 1.0) number: Int
    ) {
        val bloodmoonWorld =
            BloodmoonRegistry.getWorld(world.name) ?: throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-bloodmoon")
                    .replace("%world%", world.name)
                    .miniToComponent()
            )
        if (bloodmoonWorld.status != BloodmoonWorld.Status.INACTIVE) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.manage-days.active")
                    .miniToComponent()
            )
        }
        if (bloodmoonWorld.activationMethod !is DaysActivation) {
            throw CommandErrorException(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.manage-days.not-days")
                    .miniToComponent()
            )
        }
        val result = bloodmoonWorld.activationMethod.dayCount - number
        bloodmoonWorld.activationMethod.dayCount -= result
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.manage-days.success")
                .replace("%world%", world.name)
                .replace("%days%", result.toString())
                .miniToComponent()
        )
    }
}
