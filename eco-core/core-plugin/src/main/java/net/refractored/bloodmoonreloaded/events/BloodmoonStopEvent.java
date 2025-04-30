package net.refractored.bloodmoonreloaded.events;

import net.refractored.bloodmoonreloaded.bloodmoon.BloodmoonWorld;
import net.refractored.bloodmoonreloaded.bloodmoon.deactivation.implementation.DeactivationMethod;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BloodmoonStopEvent extends BloodmoonEvent implements Cancellable {

    public BloodmoonStopEvent(BloodmoonWorld world, StopCause cause) {
        super(world);
        this.cause = cause;
    }

    Boolean cancelled = false;

    StopCause cause;

    public StopCause getCause() {
        return cause;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public enum StopCause {
        /**
         * The bloodmoon was stopped by another plugin
         */
        PLUGIN,

        /**
         * The bloodmoon was stopped by a command.
         */
        COMMAND,

        /**
         * The bloodmoon was stopped because the deactivation method conditions were met.
         *
         * @see DeactivationMethod
         */
        METHOD,

        /**
         * The bloodmoon was stopped because the world was unloaded.
         */
        UNLOAD,

        /**
         * The bloodmoon was stopped because the plugin was reloaded.
         */
        RELOAD,

        RESTART
    }
}
