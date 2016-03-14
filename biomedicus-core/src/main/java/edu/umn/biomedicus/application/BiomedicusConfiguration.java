package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.settings.Settings;

import java.nio.file.Path;

/**
 * Global biomedicus configuration object.
 *
 * @since 1.5.0
 */
public interface BiomedicusConfiguration {
    /**
     * Gets the global biomedicus settings, loaded from java runtime parameters and the biomedicus.properties file.
     *
     * @return settings object
     */
    Settings getSettings();

    /**
     * Gets the path to the data directory.
     *
     * @return path to data directory.
     */
    Path getDataDir();

    /**
     * Gets the path to the config directory.
     *
     * @return path to config directory.
     */
    Path getConfigDir();

    /**
     * Gets the path to a data file specified in a settings key.
     *
     * @param settingsKey the settings key to retrieve the path from.
     * @return path to data file.
     */
    default Path resolveDataFile(String settingsKey) {
        Path path = getSettings().getAsPath(settingsKey);
        if (!path.isAbsolute()) {
            path = getDataDir().resolve(path);
        }
        return path;
    };
}
