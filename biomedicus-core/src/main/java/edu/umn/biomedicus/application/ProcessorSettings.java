package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.settings.Settings;

import java.nio.file.Path;

/**
 * Biomedicus processor settings in the current document processor context.
 *
 * @since 1.5.0
 */
public interface ProcessorSettings {
    /**
     * Returns settings for the current document processor context.
     *
     * @return settings object.
     */
    Settings getSettings();

    default Path getSettingAsPath(String key) {
        return getSettings().getAsPath(key);
    }

    default double getSettingAsDouble(String key) {
        return getSettings().getAsDouble(key);
    }

    default float getSettingAsFloat(String key) {
        return getSettings().getAsFloat(key);
    }

    default long getSettingAsLong(String key) {
        return getSettings().getAsLong(key);
    }

    default int getSettingAsInt(String key) {
        return getSettings().getAsInt(key);
    }

    default boolean getSettingAsBoolean(String key) {
        return getSettings().getAsBoolean(key);
    }

    default boolean containsSetting(String key) {
        return getSettings().containsSetting(key);
    }

    default String getSetting(String key) {
        return getSettings().get(key);
    }
}
