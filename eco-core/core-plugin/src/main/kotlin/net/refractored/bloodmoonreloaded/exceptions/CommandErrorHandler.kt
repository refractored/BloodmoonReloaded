package net.refractored.bloodmoonreloaded.exceptions

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.exception.*
import revxrsal.commands.bukkit.util.BukkitUtils
import revxrsal.commands.command.CommandActor
import revxrsal.commands.exception.*
import revxrsal.commands.node.ParameterNode
import java.util.*

class CommandErrorHandler :  BukkitExceptionHandler() {
    @HandleException
    override fun onInvalidPlayer(e: InvalidPlayerException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-player")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    @HandleException
    override fun onInvalidWorld(e: InvalidWorldException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-world")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    @HandleException
    override fun onInvalidWorld(e: MissingLocationParameterException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-location")
                .replace("%argument%", e.input())
                .miniToComponent()
        )
    }

    @HandleException
    override fun onSenderNotConsole(e: SenderNotConsoleException?, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.not-console")
                .miniToComponent()
        )
    }

    @HandleException
    override fun onSenderNotPlayer(e: SenderNotPlayerException?, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.not-player")
                .miniToComponent()
        )
    }

    @HandleException
    override fun onMalformedEntitySelector(e: MalformedEntitySelectorException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-entity-selector")
                .replace("%input%", e.input())
                .replace("%error%", e.errorMessage())
                .miniToComponent()
        )
    }

    @HandleException
    override fun onNonPlayerEntities(e: NonPlayerEntitiesException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.more-than-one-entity")
                .miniToComponent()
        )
    }

    @HandleException
    override fun onMoreThanOneEntity(e: MoreThanOneEntityException?, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.more-than-one-entity")
                .miniToComponent()
        )
    }

    @HandleException
    override fun onEmptyEntitySelector(e: EmptyEntitySelectorException?, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.no-entities-found")
                .miniToComponent()
        )
    }

    override fun onEnumNotFound(e: EnumNotFoundException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-choice")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    override fun onExpectedLiteral(e: ExpectedLiteralException, actor: BukkitCommandActor) {
        BloodmoonPlugin.instance.langYml
            .getStringPrefixed("messages.general.expected-string-literal")
            .replace("%input%", e.input())
            .replace("%literal%", e.node<CommandActor>().name())
            .miniToComponent()
    }

    override fun onInputParse(e: InputParseException, actor: BukkitCommandActor) {
        when (e.cause()) {
            InputParseException.Cause.INVALID_ESCAPE_CHARACTER -> actor.reply(BloodmoonPlugin.instance.langYml.getStringPrefixed("messages.general.invalid-escape-character").miniToComponent())
            InputParseException.Cause.UNCLOSED_QUOTE -> actor.reply(BloodmoonPlugin.instance.langYml.getStringPrefixed("messages.general.unclosed-quote").miniToComponent())
            InputParseException.Cause.EXPECTED_WHITESPACE -> actor.reply(BloodmoonPlugin.instance.langYml.getStringPrefixed("messages.general.expected-whitespace").miniToComponent())
        }
    }

    override fun onInvalidListSize(e: InvalidListSizeException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor?, *>) {
        if (e.inputSize() < e.minimum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.list-too-small")
                    .replace("%parameter%", parameter.name())
                    .replace("%minimum%", fmt(e.minimum()))
                    .miniToComponent()
            )
        }

        if (e.inputSize() > e.maximum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.list-too-large")
                    .replace("%parameter%", parameter.name())
                    .replace("%maximum%", fmt(e.maximum()))
                    .miniToComponent()
            )        }
    }

    override fun onInvalidStringSize(e: InvalidStringSizeException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor?, *>) {
        if (e.input().length < e.minimum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.string-too-small")
                    .replace("%parameter%", parameter.name())
                    .replace("%minimum%", fmt(e.minimum()))
                    .miniToComponent()
            )
        }

        if (e.input().length > e.maximum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.string-too-long")
                    .replace("%parameter%", parameter.name())
                    .replace("%minimum%", fmt(e.minimum()))
                    .miniToComponent()
            )
        }
    }

    override fun onInvalidBoolean(e: InvalidBooleanException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-boolean")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    override fun onInvalidDecimal(e: InvalidDecimalException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-number")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    override fun onInvalidInteger(e: InvalidIntegerException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-integer")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    override fun onInvalidUUID(e: InvalidUUIDException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-uuid")
                .replace("%input%", e.input())
                .miniToComponent()
        )
    }

    override fun onMissingArgument(e: MissingArgumentException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor?, *>) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.missing-argument")
                .replace("%argument%", parameter.name())
                .replace("%command%", parameter.command().usage())
                .miniToComponent()
        )
    }

    override fun onNoPermission(e: NoPermissionException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.no-permission")
                .miniToComponent()
        )
    }

    override fun onNumberNotInRange(e: NumberNotInRangeException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor?, Number?>) {
        if (e.input().toDouble() < e.minimum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.number-too-small")
                    .replace("%parameter%", parameter.name())
                    .replace("%input%", fmt(e.input()))
                    .replace("%minimum%", fmt(e.minimum()))
                    .miniToComponent()
            )
        }

        if (e.input().toDouble() > e.maximum()) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.number-too-large")
                    .replace("%parameter%", parameter.name())
                    .replace("%input%", fmt(e.input()))
                    .replace("%minimum%", fmt(e.minimum()))
                    .miniToComponent()
            )
        }
    }

    override fun onInvalidHelpPage(e: InvalidHelpPageException, actor: BukkitCommandActor) {
        if (e.numberOfPages() == 1) {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-help-single")
                    .replace("%input%", fmt(e.page()))
                    .miniToComponent()
            )
        } else {
            actor.reply(
                BloodmoonPlugin.instance.langYml
                    .getStringPrefixed("messages.general.invalid-help-single")
                    .replace("%input%", fmt(e.page()))
                    .replace("%pages%", fmt(e.numberOfPages()))
                    .miniToComponent()
            )
        }
    }

    override fun onUnknownCommand(e: UnknownCommandException, actor: BukkitCommandActor) {
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.unknown-command")
                .replace("%command%", e.input())
                .miniToComponent()
        )
    }
}
