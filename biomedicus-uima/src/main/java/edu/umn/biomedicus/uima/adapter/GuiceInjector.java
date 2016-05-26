package edu.umn.biomedicus.uima.adapter;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.application.BiomedicusScopes;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.Resource_ImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice injector resource implementation.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class GuiceInjector extends Resource_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceInjector.class);

    private final Injector injector;

    public GuiceInjector() {
        LOGGER.info("Initializing Guice Injector Resource");
        try {
            injector = Bootstrapper.create(new UimaModule()).injector();
        } catch (BiomedicusException e) {
            throw new IllegalStateException(e);
        }
    }

    public Injector getInjector() {
        return injector;
    }
}
