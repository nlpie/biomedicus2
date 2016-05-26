package edu.umn.biomedicus.uima.adapter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.application.BiomedicusScopes;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.type.TermTokenAnnotation;
import edu.umn.biomedicus.uima.labels.DefaultLabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.UimaLabeler;
import edu.umn.biomedicus.uima.labels.UimaLabels;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;

public class UimaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JCas.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(ProcessorScoped.class);
        bind(CAS.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(ProcessorScoped.class);

        bind(Labels.class).to(UimaLabels.class).in(DocumentScoped.class);
        bind(Labeler.class).to(UimaLabeler.class).in(DocumentScoped.class);
    }

    @Provides @Singleton
    LabelAdapter<TermToken> termTokenLabelAdapter() {
        return DefaultLabelAdapter.create(TermTokenAnnotation.class, (t, u) -> {}, (annotation) -> TermToken.TERM_TOKEN);
    }
}
