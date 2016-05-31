package edu.umn.biomedicus.application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;

import java.nio.file.Path;

/**
 *
 */
public class GlobalSettings {
    private final Injector injector;

    @Inject
    public GlobalSettings(Injector injector) {
        this.injector = injector;
    }

    private <T> T getSetting(String key, Class<T> tClass) {
        Key<T> guiceKey = Key.get(tClass, new SettingImpl(key));
        return injector.getInstance(guiceKey);
    }

    public <T> T getInstance(String key, Class<T> tClass) {
        String implementationKey = injector.getInstance(Key.get(String.class, new SettingImpl(key)));
        return getSetting(key, tClass);
    }

    public String getString(String key) {
        return getSetting(key, String.class);
    }

    public Integer getInteger(String key) {
        return getSetting(key, Integer.class);
    }

    public Path getPath(String key) {
        return getSetting(key, Path.class);
    }

    public Double getSetting(String key) {
        return getSetting(key, Double.class);
    }
}
