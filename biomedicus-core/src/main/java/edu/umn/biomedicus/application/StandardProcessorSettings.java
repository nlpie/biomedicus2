package edu.umn.biomedicus.application;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.ProcessorSettings;
import edu.umn.biomedicus.common.settings.Settings;

import java.nio.file.Path;

/**
 *
 */
@DocumentScoped
class StandardProcessorSettings implements ProcessorSettings {
    private final Settings settings;

    StandardProcessorSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String get(String key) {
        return settings.get(key);
    }

    @Override
    public boolean containsSetting(String key) {
        return settings.containsSetting(key);
    }

    @Override
    public boolean getAsBoolean(String key) {
        return settings.getAsBoolean(key);
    }

    @Override
    public int getAsInt(String key) {
        return settings.getAsInt(key);
    }

    @Override
    public long getAsLong(String key) {
        return settings.getAsLong(key);
    }

    @Override
    public float getAsFloat(String key) {
        return settings.getAsFloat(key);
    }

    @Override
    public double getAsDouble(String key) {
        return settings.getAsDouble(key);
    }

    @Override
    public Path getAsPath(String key) {
        return settings.getAsPath(key);
    }
}
