package net.refractored.drops.listeners

import net.refractored.bloodmoonreloaded.registry.BloodmoonRegistry
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import net.refractored.drops.DropsExtension
import net.refractored.hordes.HordesExtension
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class OnEntityDeath : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun run(event: EntityDeathEvent) {
        val bloodWorld = BloodmoonRegistry.getWorld(event.entity.world.name) ?: return

        if (bloodWorld.status != BloodmoonWorld.Status.ACTIVE) {
            return
        }

        val dropsConfig = DropsExtension.instance.configHandler.getSection(event.entity.world) ?: return

        if (dropsConfig.entities != null && dropsConfig.entities.none { it.matches(event.entity) }) {
            return
        }

        if (dropsConfig.configSection.getBoolean("disable-vanilla-drops")) {
            event.drops.clear()
        }

        if (event.entity.location.y > dropsConfig.configSection.getInt("farm-prevention.max-y")) {
            return
        }

        if (dropsConfig.configSection.getBoolean("farm-prevention.killed-by-player") && event.entity.killer !is Player) {
            return
        }

        if (dropsConfig.configSection.getBoolean("farm-prevention.line-of-sight") &&
            event.entity.killer?.let(event.entity::hasLineOfSight) != true
        ) {
            return
        }

        val isHorde: Boolean = getHordeInfo(event.entity)

        for (i in 0 until dropsConfig.configSection.getInt("max-amount")) {
            val itemStack =
                if (i < dropsConfig.configSection.getInt("min-amount")) {
                    dropsConfig.getRandomItemstack(isHorde)
                } else {
                    dropsConfig.tryRandomItemstack(isHorde) ?: continue
                }
            event.entity.world.dropItemNaturally(event.entity.location, itemStack)
        }
    }

    private fun getHordeInfo(entity: Entity): Boolean {
        if (DropsExtension.instance.hordes == null) return false
        val config = HordesExtension.instance.configHandler.getSection(entity.world) ?: return false
        return entity.persistentDataContainer.has(config.pdcKey)
    }
}
