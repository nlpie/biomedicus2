package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorScoped;

/**
 *
 */
class BiomedicusModule extends AbstractModule {
    @Override
    protected void configure() {
        bindScope(DocumentScoped.class, BiomedicusScopes.DOCUMENT_SCOPE);
        bindScope(ProcessorScoped.class, BiomedicusScopes.PROCESSOR_SCOPE);
    }
}
