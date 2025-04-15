package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.World

/**
 * Registry for Bloodmoon worlds.
 */
object TypeRegistry {

    private val registry: MutableMap<String, BloodmoonWorldFactory> = mutableMapOf()

    fun registerType(type: String, factory: BloodmoonWorldFactory) {
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

    fun getType(type: String): BloodmoonWorldFactory {
        return registry[type] ?: throw IllegalArgumentException("Bloodmoon type \"$type\" not found.")
    }

    interface BloodmoonWorldFactory {
        fun create(world: World, config: Config): BloodmoonWorld
    }

}
