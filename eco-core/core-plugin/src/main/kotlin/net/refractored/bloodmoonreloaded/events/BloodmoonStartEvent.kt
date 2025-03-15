package net.refractored.bloodmoonreloaded.events

import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld
import org.bukkit.World
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BloodmoonStartEvent(
    /**
     * The world that the bloodmoon is starting in
     */
    val world: World,
    /**
     * The BloodmoonWorld that the bloodmoon is starting in
     */
    val bloodmoonWorld: BloodmoonWorld
) : Event(),
    Cancellable {
    private var cancelled = false

    override fun getHandlers() = getHandlerList()

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        private val handlerList = HandlerList()

        /**
         * Get the handler list for this event
         */
        fun getHandlerList(): HandlerList = handlerList
    }
}
