package net.refractored.hordes.commands

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.exceptions.CommandErrorException
import net.refractored.bloodmoonreloaded.messages.Messages.getStringPrefixed
import net.refractored.bloodmoonreloaded.messages.Messages.miniToComponent
import net.refractored.bloodmoonreloaded.messages.Messages.replace
import net.refractored.hordes.HordesExtension
import net.refractored.hordes.util.EligibleUtil.getEligiblePlayers
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class SpawnHordeCommand {
    @CommandPermission("bloodmoon.admin.horde.spawn")
    @Description("Spawns a horde on a player.")
    @Command("bloodmoon spawn horde")
    fun execute(
        actor: BukkitCommandActor,
        @Optional player: Player = actor.requirePlayer().world.getEligiblePlayers().randomOrNull() ?: throw CommandErrorException(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.spawn.horde.no-players")
                .miniToComponent(),
        )
        ,
        @Optional announce: Boolean = true,
    ) {
        HordesExtension.instance.configHandler.getSection(player.world)?.spawnHorde(player, announce) ?: throw CommandErrorException(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.general.invalid-horde")
                .miniToComponent(),
        )
        actor.reply(
            BloodmoonPlugin.instance.langYml
                .getStringPrefixed("messages.spawn.horde.success")
                .replace("%player%", player.displayName())
                .miniToComponent(),
        )
    }
}
