package net.refractored.bloodmoonreloaded.events;

import net.refractored.bloodmoonreloaded.types.implementation.BloodmoonWorld;
import org.bukkit.World;
import org.bukkit.event.Event;

abstract public class BloodmoonEvent extends Event {

    public BloodmoonEvent(final BloodmoonWorld world) {
        this.bloodmoonWorld = world;
    }

    BloodmoonWorld bloodmoonWorld;

    public BloodmoonWorld getBloodmoonWorld() {
        return bloodmoonWorld;
    }

    public World getWorld() {
        return bloodmoonWorld.getWorld();
    }
}
