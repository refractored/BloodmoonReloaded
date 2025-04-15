package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

/**
 * Registry for Bloodmoon worlds.
 */
object TypeRegistry {

    private val registry: MutableMap<String, (world: World, config: Config) -> BloodmoonWorld> = mutableMapOf()

    fun registerType(type: String, factory: (world: World, config: Config) -> BloodmoonWorld) {
        if (registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon type $type is already registered.")
        }
        registry[type] = factory
    }

    fun unregisterType(type: String) {
        if (!registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon type $type is not registered.")
        }
        registry.remove(type)
    }

    fun getType(type: String, world: World, config: Config):BloodmoonWorld {
        return registry[type]?.invoke(world,config) ?: throw IllegalArgumentException("Bloodmoon type \"$type\" not found.")
    }
}
