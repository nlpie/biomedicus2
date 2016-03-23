package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;

/**
 *
 */
public class ProcessorSettingsModule extends AbstractModule {

    private final ProcessorSettings processorSettings;

    public ProcessorSettingsModule(ProcessorSettings processorSettings) {
        this.processorSettings = processorSettings;
    }

    @Override
    protected void configure() {
        bind(ProcessorSettings.class).toInstance(processorSettings);
    }
}
