package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.common.text.Document;

/**
 *
 */
class BiomedicusModule extends AbstractModule {
    @Override
    protected void configure() {
        bindScope(DocumentScoped.class, BiomedicusScopes.DOCUMENT_SCOPE);
        bindScope(ProcessorScoped.class, BiomedicusScopes.PROCESSOR_SCOPE);

        bind(Document.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(BiomedicusScopes.DOCUMENT_SCOPE);
    }
}
