package net.refractored.bloodmoonreloaded.worlds

/**
 * Represents an active bloodmoon.
 */
class ActiveBloodmoon(
    val bloodmoonWorld: BloodmoonWorld,
    /**
     * The length in milliseconds of the bloodmoon.
     */
    var length: Long = bloodmoonWorld.length,
) {
    var fullTime: Long = bloodmoonWorld.world.fullTime

    /**
     * The time the bloodmoon expires.
     */
    val expiryTime = System.currentTimeMillis() + length

    init {
        bloodmoonWorld.savedBloodmoonRemainingMillis = length.toDouble()
    }
}
