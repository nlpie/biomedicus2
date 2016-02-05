package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.settings.Settings;

import java.nio.file.Path;

/**
 *
 */
public interface BiomedicusConfiguration {
    Settings getSettings();

    Path getDataDir();

    default Path resolveDataFile(String settingsKey) {
        Path path = getSettings().getAsPath(settingsKey);
        if (!path.isAbsolute()) {
            path = getDataDir().resolve(path);
        }
        return path;
    };
}
