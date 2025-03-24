package net.refractored.bloodmoonreloaded.exceptions

import net.kyori.adventure.text.ComponentLike
import net.refractored.bloodmoonreloaded.messages.Messages.toPlaintext
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.command.CommandActor
import revxrsal.commands.exception.SendableException

class CommandErrorException(
    val component: ComponentLike,
) : SendableException() {
    override fun sendTo(actor: CommandActor) {
        if (actor is BukkitCommandActor) {
            actor.reply(this.component)
        } else {
            actor.reply(this.component.asComponent().toPlaintext())
        }
    }
}
