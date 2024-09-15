package net.refractored.bloodmoonreloaded.exceptions

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.util.MessageUtil.getStringPrefixed
import net.refractored.bloodmoonreloaded.util.MessageUtil.miniToComponent
import revxrsal.commands.bukkit.sender
import revxrsal.commands.command.CommandActor
import revxrsal.commands.exception.*

class CommandErrorHandler : DefaultExceptionHandler() {
    override fun invalidNumber(
        actor: CommandActor,
        exception: InvalidNumberException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.invalid-number")
                .replace("%input%", exception.input)
                .miniToComponent(),
        )
    }

    override fun invalidSubcommand(
        actor: CommandActor,
        exception: InvalidSubcommandException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.invalid-subcommand")
                .replace("%input%", exception.input)
                .miniToComponent(),
        )
    }

    override fun invalidBoolean(
        actor: CommandActor,
        exception: InvalidBooleanException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.invalid-boolean")
                .replace("%input%", exception.input)
                .miniToComponent(),
        )
    }

    override fun cooldown(
        actor: CommandActor,
        exception: CooldownException,
    ) {
        actor.errorLocalized("OnCooldown", formatTimeFancy(exception.timeLeftMillis))
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.on-cooldown")
                .replace("%time%", formatTimeFancy(exception.timeLeftMillis))
                .miniToComponent(),
        )
    }

    override fun numberNotInRange(
        actor: CommandActor,
        exception: NumberNotInRangeException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.number-not-in-range")
                .replace("%input%", FORMAT.format(exception.input))
                .replace("%min%", FORMAT.format(exception.minimum))
                .replace("%max%", FORMAT.format(exception.maximum))
                .miniToComponent(),
        )
    }

    override fun invalidEnumValue(
        actor: CommandActor,
        exception: EnumNotFoundException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.invalid-enum")
                .replace("%input%", exception.input)
                .replace("%parameter%", exception.parameter.name)
                .miniToComponent(),
        )
    }

    override fun commandInvocation(
        actor: CommandActor,
        exception: CommandInvocationException,
    ) {
        exception.cause.printStackTrace()
    }

    override fun tooManyArguments(
        actor: CommandActor,
        exception: TooManyArgumentsException,
    ) {
        val command = exception.command
        val usage = (command.path.toRealString() + " " + command.usage).trim { it <= ' ' }
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.too-many-arguments")
                .replace("%usage%", usage)
                .miniToComponent(),
        )
    }

    override fun invalidCommand(
        actor: CommandActor,
        exception: InvalidCommandException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.invalid-command")
                .replace("%input%", exception.input)
                .miniToComponent(),
        )
    }

    override fun noSubcommandSpecified(
        actor: CommandActor,
        exception: NoSubcommandSpecifiedException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.no-subcommand-specified")
                .miniToComponent(),
        )
    }

    override fun missingArgument(
        actor: CommandActor,
        exception: MissingArgumentException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.missing-argument")
                .replace("%parameter%", exception.parameter.name)
                .miniToComponent(),
        )
    }

    override fun noPermission(
        actor: CommandActor,
        exception: NoPermissionException,
    ) {
        actor.sender.sendMessage(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.no-permission")
                .miniToComponent(),
        )
    }
}
