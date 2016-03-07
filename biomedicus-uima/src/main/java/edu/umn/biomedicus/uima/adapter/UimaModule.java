package edu.umn.biomedicus.uima.adapter;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.application.BiomedicusScopes;
import edu.umn.biomedicus.common.text.Document;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;

/**
 * Uima module.
 *
 * @since 1.4
 */
class UimaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CAS.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(BiomedicusScopes.DOCUMENT_SCOPE);
        bind(JCas.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(BiomedicusScopes.DOCUMENT_SCOPE);

        bind(Document.class).to(JCasDocument.class);
    }
}
