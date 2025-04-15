package net.refractored.bloodmoonreloaded.events;

import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BloodmoonStartEvent extends BloodmoonEvent implements Cancellable {

    public BloodmoonStartEvent(BloodmoonWorld world) {
        super(world);
    }

    Boolean cancelled = false;

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

}
