package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.settings.Settings;

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
}
