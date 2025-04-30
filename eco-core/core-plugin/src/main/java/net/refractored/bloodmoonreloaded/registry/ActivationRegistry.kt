package net.refractored.bloodmoonreloaded.registry

import com.willfp.eco.core.config.interfaces.Config
import net.refractored.bloodmoonreloaded.types.activation.implementation.ActivationMethod
import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.World

/**
 * Registry for Bloodmoon worlds.
 */
object ActivationRegistry {

    private val registry: MutableMap<String, (bloodmoonWorld: BloodmoonWorld) -> ActivationMethod> = mutableMapOf()

    fun registerType(type: String, factory: (bloodmoonWorld: BloodmoonWorld) -> ActivationMethod) {
        if (registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon activation type $type is already registered.")
        }
        registry[type] = factory
    }

    fun unregisterType(type: String) {
        if (!registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon activation type $type is not registered.")
        }
        registry.remove(type)
    }

    fun getType(type: String, bloodmoonWorld: BloodmoonWorld): ActivationMethod {
        return registry[type]?.invoke(bloodmoonWorld) ?: throw IllegalArgumentException("Bloodmoon activation type \"$type\" not found.")
    }
}
