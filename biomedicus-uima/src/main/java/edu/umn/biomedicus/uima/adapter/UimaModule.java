package edu.umn.biomedicus.uima.adapter;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.application.BiomedicusScopes;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;

final class UimaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JCas.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(ProcessorScoped.class);
        bind(CAS.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(ProcessorScoped.class);
    }
}
