package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umn.biomedicus.application.BiomedicusModule;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.sections.SectionsModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.resource.Resource_ImplBase;

/**
 * Guice injector resource implementation.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class GuiceInjector extends Resource_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Injector injector;

    public GuiceInjector() {
        LOGGER.info("Initializing Guice Injector Resource");
        injector = new Bootstrapper(new UimaModule()).injector();
    }

    public Injector getInjector() {
        return injector;
    }
}
