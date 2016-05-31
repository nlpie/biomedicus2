package edu.umn.biomedicus.uima.labels;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.type.*;

public final class LabelsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<Labels<TermToken>>(){}).to(new TypeLiteral<UimaLabels<TermToken>>(){})
                .in(DocumentScoped.class);
        bind(new TypeLiteral<Labeler<TermToken>>(){}).to(new TypeLiteral<UimaLabeler<TermToken>>(){})
                .in(DocumentScoped.class);

        bind(new TypeLiteral<Labels<Acronym>>(){}).to(new TypeLiteral<UimaLabels<Acronym>>(){})
                .in(DocumentScoped.class);
        bind(new TypeLiteral<Labeler<Acronym>>(){}).to(new TypeLiteral<UimaLabeler<Acronym>>(){})
                .in(DocumentScoped.class);

        bind(new TypeLiteral<Labels<AcronymExpansion>>(){}).to(new TypeLiteral<UimaLabels<AcronymExpansion>>(){})
                .in(DocumentScoped.class);
        bind(new TypeLiteral<Labeler<AcronymExpansion>>(){}).to(new TypeLiteral<UimaLabeler<AcronymExpansion>>(){})
                .in(DocumentScoped.class);

        bind(new TypeLiteral<Labels<WordIndex>>(){}).to(new TypeLiteral<UimaLabels<WordIndex>>(){})
                .in(DocumentScoped.class);
        bind(new TypeLiteral<Labeler<WordIndex>>(){}).to(new TypeLiteral<UimaLabeler<WordIndex>>(){})
                .in(DocumentScoped.class);

        bind(new TypeLiteral<Labels<NormIndex>>(){}).to(new TypeLiteral<UimaLabels<NormIndex>>(){})
                .in(DocumentScoped.class);
        bind(new TypeLiteral<Labeler<NormIndex>>(){}).to(new TypeLiteral<UimaLabeler<NormIndex>>(){})
                .in(DocumentScoped.class);
    }

    @Provides
    @Singleton
    LabelAdapter<TermToken> termTokenLabelAdapter() {
        return DefaultLabelAdapter.create(TermTokenAnnotation.class,
                (termToken, annotation) -> {

                },
                annotation -> TermToken.TERM_TOKEN
        );
    }

    @Provides
    @Singleton
    LabelAdapter<Acronym> acronymLabelAdapter() {
        return DefaultLabelAdapter.create(AcronymAnnotation.class,
                (acronym, annotation) -> {

                },
                acronymAnnotation -> Acronym.ACRONYM
        );
    }

    @Provides
    @Singleton
    LabelAdapter<AcronymExpansion> acronymExpansionLabelAdapter() {
        return DefaultLabelAdapter.create(
                AcronymExpansionAnnotation.class,
                (acronymExpansion, annotation) -> {
                    annotation.setLongform(acronymExpansion.longform());
                },
                annotation -> new AcronymExpansion(annotation.getLongform())
        );
    }

    @Provides
    @Singleton
    LabelAdapter<WordIndex> wordIndexLabelAdapter() {
        return DefaultLabelAdapter.create(
                WordIndexAnnotation.class,
                (wordIndex, annotation) -> {
                    annotation.setIndex(wordIndex.term().indexedTerm());
                },
                annotation -> new WordIndex(new IndexedTerm(annotation.getIndex()))
        );
    }

    @Provides
    @Singleton
    LabelAdapter<NormIndex> normIndexLabelAdapter() {
        return DefaultLabelAdapter.create(
                NormIndexAnnotation.class,
                (normIndex, annotation) -> {
                    annotation.setIndex(normIndex.term().indexedTerm());
                },
                annotation -> new NormIndex(new IndexedTerm(annotation.getIndex()))
        );
    }
}
