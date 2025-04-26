package net.refractored.bloodmoonreloaded.extensions;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Section {
    @NotNull List< @NotNull World> getWorlds();
}

