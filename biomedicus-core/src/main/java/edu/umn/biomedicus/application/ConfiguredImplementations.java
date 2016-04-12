package edu.umn.biomedicus.application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 *
 */
@Singleton
public class ConfiguredImplementations {

    private final Injector injector;

    @Inject
    public ConfiguredImplementations(Injector injector) {
        this.injector = injector;
    }

    public <T> T getInstance(String settingsKey, Class<T> tClass) {
        String implementationKey = injector.getInstance(Key.get(String.class, Names.named(settingsKey)));
        return injector.getInstance(Key.get(tClass, Names.named(implementationKey)));
    }
}
