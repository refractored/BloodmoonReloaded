package net.refractored.bloodmoonreloaded.bloodmoon

import net.kyori.adventure.text.ComponentLike

interface LifecycleMethod{
    val bloodmoonWorld: BloodmoonWorld

    /**
     * Returns true if method conditions are true
     */
    fun checkStatus(): Boolean

    fun getInfo(): ComponentLike

    fun onActivation() {}

    fun onDeactivation() {}

    fun periodicTasks() {}

    /**
     * Save data to the database if applicable
     */
    fun saveData() {}
}
