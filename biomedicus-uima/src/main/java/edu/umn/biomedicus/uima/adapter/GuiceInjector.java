package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.resource.Resource_ImplBase;

import java.io.IOException;

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
        try {
            injector = new Bootstrapper(new UimaModule()).injector();
        } catch (BiomedicusException e) {
            throw new IllegalStateException(e);
        }
    }

    public Injector getInjector() {
        return injector;
    }
}
