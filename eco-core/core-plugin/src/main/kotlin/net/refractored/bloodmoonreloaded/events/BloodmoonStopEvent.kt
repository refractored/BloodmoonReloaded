package net.refractored.bloodmoonreloaded.events

import net.refractored.bloodmoonreloaded.worlds.BloodmoonWorld
import org.bukkit.World
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BloodmoonStopEvent(
    /**
     * The world that the bloodmoon is starting in
     */
    val World: World,
    /**
     * The BloodmoonWorld that the bloodmoon is starting in
     */
    val BloodmoonWorld: BloodmoonWorld,
    /**
     * The cause of the bloodmoon stopping
     */
    val cause: StopCause
) : Event(),
    Cancellable {
    private var cancelled = false

    override fun getHandlers() = getHandlerList()

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        /**
         * Get the handler list for this event
         */
        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    enum class StopCause {
        /**
         * The bloodmoon was stopped by another plugin
         */
        PLUGIN,

        /**
         * The bloodmoon was stopped by a command.
         */
        COMMAND,

        /**
         * The bloodmoon was because the timer was over.
         */
        TIMER,

        /**
         * The bloodmoon was stopped because the world was unloaded.
         */
        UNLOAD,

        /**
         * The bloodmoon was stopped because the plugin was reloaded.
         */
        RELOAD
    }
}
