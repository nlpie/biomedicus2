package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import com.google.inject.Key;

import java.util.Collection;

/**
 *
 */
public class ProcessorSettingsModule extends AbstractModule {
    private final Collection<Key<?>> processorSettings;

    public ProcessorSettingsModule(Collection<Key<?>> processorSettings) {
        this.processorSettings = processorSettings;
    }

    @Override
    protected void configure() {
        processorSettings.forEach(this::bindToScope);
    }

    private <T> void bindToScope(Key<T> key) {
        bind(key).toProvider(BiomedicusScopes.providedViaSeeding()).in(BiomedicusScopes.PROCESSOR_SCOPE);
    }
}
