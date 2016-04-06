package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.common.settings.Settings;

/**
 *
 */
public class ProcessorSettingsModule extends AbstractModule {
    private final ProcessorSettings processorSettings;

    public ProcessorSettingsModule(Settings processorSettings) {
        this.processorSettings = new StandardProcessorSettings(processorSettings);
    }

    @Override
    protected void configure() {
        bind(ProcessorSettings.class).toInstance(processorSettings);
    }
}
