package net.refractored.bloodmoonreloaded.registry

import net.refractored.bloodmoonreloaded.bloodmoon.deactivation.DeactivationMethod
import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld

/**
 * Registry for Bloodmoon worlds.
 */
object DeactivationRegistry {

    private val registry: MutableMap<String, (bloodmoonWorld: BloodmoonWorld) -> DeactivationMethod> = mutableMapOf()

    fun registerType(type: String, factory: (bloodmoonWorld: BloodmoonWorld) -> DeactivationMethod) {
        if (registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon deactivation type $type is already registered.")
        }
        registry[type] = factory
    }

    fun unregisterType(type: String) {
        if (!registry.containsKey(type)) {
            throw IllegalArgumentException("Bloodmoon deactivation type $type is not registered.")
        }
        registry.remove(type)
    }

    fun getType(type: String, bloodmoonWorld: BloodmoonWorld): DeactivationMethod {
        return registry[type]?.invoke(bloodmoonWorld) ?: throw IllegalArgumentException("Bloodmoon activation type \"$type\" not found.")
    }
}
