package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.annotations.DocumentScoped;

/**
 *
 */
public class BiomedicusModule extends AbstractModule {
    @Override
    protected void configure() {
        bindScope(DocumentScoped.class, BiomedicusScopes.DOCUMENT_SCOPE);

        bind(BiomedicusConfiguration.class).to(DefaultConfiguration.class).asEagerSingleton();
    }
}
