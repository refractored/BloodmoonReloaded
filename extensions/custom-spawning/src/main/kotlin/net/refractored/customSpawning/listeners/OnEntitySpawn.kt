package net.refractored.customSpawning.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld.Status
import net.refractored.customSpawning.CustomSpawningExtension
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent

class OnEntitySpawn : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBloodmoonStart(event: CreatureSpawnEvent) {
        val bloodmoonWorld = BloodmoonRegistry.getWorld(event.location.world.name) ?: return

        if (bloodmoonWorld.status != Status.ACTIVE) return

        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return

        val spawnConfig = CustomSpawningExtension.instance.configHandler.getSection(event.location.world) ?: return

        if (event.entity !is Monster && !spawnConfig.replaceAllMobs) return

        event.isCancelled = true

        if (spawnConfig.strikeLightning) {
            event.location.world.strikeLightningEffect(event.location)
        }

        spawnConfig.getWeightedMob().spawn(event.location)
    }
}
