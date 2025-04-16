package net.refractored.hordes.listeners

import net.refractored.bloodmoonreloaded.BloodmoonPlugin
import net.refractored.bloodmoonreloaded.events.BloodmoonStartEvent
import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.hordes.HordesExtension
import net.refractored.hordes.hordes.HordeConfig
import net.refractored.hordes.util.EligibleUtil.getEligiblePlayers
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class OnBloodmoonStart : Listener {
    @EventHandler
    fun onBloodmoonStart(event: BloodmoonStartEvent) {
        val hordeConfig = HordesExtension.instance.configHandler.getSection(event.world) ?: return
        scheduleBloodmoonTask(event, hordeConfig)
    }

    private fun scheduleBloodmoonTask(
        event: BloodmoonStartEvent,
        hordeConfig: HordeConfig
    ) {
        BloodmoonPlugin.instance.scheduler.runLater(
            (hordeConfig.minTickTime..hordeConfig.maxTickTime).random()
        ) {
            bloodmoonTask(event, hordeConfig)
        }
    }

    private fun bloodmoonTask(
        event: BloodmoonStartEvent,
        hordeConfig: HordeConfig
    ) {
        val bloodmoonWorld = BloodmoonRegistry.getWorld(event.world.name) ?: return

        if (bloodmoonWorld.status != BloodmoonWorld.Status.ACTIVE) {
            return
        }

        val player = event.world.getEligiblePlayers().randomOrNull()

        if (player != null) {
            hordeConfig.spawnHorde(player, true)
        }

        scheduleBloodmoonTask(event, hordeConfig)
    }
}
