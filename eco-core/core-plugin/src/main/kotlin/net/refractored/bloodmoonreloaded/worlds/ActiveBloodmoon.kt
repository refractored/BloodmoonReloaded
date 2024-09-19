package net.refractored.bloodmoonreloaded.worlds

/**
 * Represents an active bloodmoon.
 */
class ActiveBloodmoon(
    val bloodmoonWorld: BloodmoonWorld,
    /**
     * The length in milliseconds of the booster.
     */
    var length: Long = bloodmoonWorld.length,
) {
    init {
        TODO()
    }
}
