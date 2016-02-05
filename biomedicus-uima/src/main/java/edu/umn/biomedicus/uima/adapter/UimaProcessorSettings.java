package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.settings.Settings;
import edu.umn.biomedicus.application.ProcessorSettings;

/**
 *
 */
@DocumentScoped
public class UimaProcessorSettings implements ProcessorSettings {
    private final Settings settings;

    public UimaProcessorSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }
}
