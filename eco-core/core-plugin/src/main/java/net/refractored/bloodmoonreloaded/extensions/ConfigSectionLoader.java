package net.refractored.bloodmoonreloaded.extensions;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Generic interface for loading a config section based on a world.
 * @param <T>
 */
public interface ConfigSectionLoader<T extends Section> {
    void removeConfig(T section);

    /**
     * Loads a config from the config file.
     * <p>
     * This will not reload the config if it is already loaded.
     *
     * @param section The section to load.
     */
    void createConfig(T section);

    /**
     * @return A read-only map of the loaded configs.
     */
    List<T> getSections();

    @Nullable
    T getSection(World world);

    /**
     * Deletes all loaded configs and populates it with the configuration in the config-file.
     */
    void refreshConfigs();
}

