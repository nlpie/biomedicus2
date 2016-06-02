package edu.umn.biomedicus.uima.labels;

import com.google.inject.*;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.type.SentenceAnnotation;
import org.apache.uima.jcas.JCas;

public final class LabelsModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @DocumentScoped
    <T> Labels<T> uimaLabelsProvider(Injector injector, JCas jCas) {
        LabelAdapter<T> labelAdapter = injector.getInstance(Key.get(new TypeLiteral<LabelAdapter<T>>() {}));
        return new UimaLabels<>(jCas, labelAdapter);
    }

    @Provides
    @Singleton
    LabelAdapter<Sentence2> sentenceLabelAdapter() {
        return DefaultLabelAdapter.create(SentenceAnnotation.class,
                (sentence2, sentenceAnnotation) -> {
                    throw new UnsupportedOperationException("Creating sentences using labeler currently unsupported.");
                },
                sentenceAnnotation -> new Sentence2());
    }

    @Provides
    @Singleton
    LabelAdapter<ParseToken> parseTokenLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.ParseToken.class,
                (parseToken, annotation) -> {
                    throw new UnsupportedOperationException("Creating parse tokens using labeler currently unsupported.");
                },
                annotation -> new ParseToken());
    }

    @Provides
    @Singleton
    LabelAdapter<PartOfSpeech> partOfSpeechLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.ParseToken.class,
                (parseToken, annotation) -> {
                    throw new UnsupportedOperationException("Creating part of speech labels using labeler currently unsupported.");
                },
                annotation -> PartOfSpeech.valueOf(annotation.getPartOfSpeech()));
    }

    @Provides
    @Singleton
    LabelAdapter<TermToken> termTokenLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.TermToken.class,
                (termToken, annotation) -> {
                    // term token has no properties
                },
                annotation -> new TermToken()
        );
    }

    @Provides
    @Singleton
    LabelAdapter<Acronym> acronymLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.Acronym.class,
                (acronym, annotation) -> {
                    // acronym has no properties
                },
                acronymAnnotation -> new Acronym()
        );
    }

    @Provides
    @Singleton
    LabelAdapter<AcronymExpansion> acronymExpansionLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.AcronymExpansion.class,
                (acronymExpansion, annotation) -> {
                    annotation.setLongform(acronymExpansion.longform());
                },
                annotation -> new AcronymExpansion(annotation.getLongform())
        );
    }

    @Provides
    @Singleton
    LabelAdapter<WordIndex> wordIndexLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.WordIndex.class,
                (wordIndex, annotation) -> {
                    annotation.setIndex(wordIndex.term().indexedTerm());
                },
                annotation -> new WordIndex(new IndexedTerm(annotation.getIndex()))
        );
    }

    @Provides
    @Singleton
    LabelAdapter<NormIndex> normIndexLabelAdapter() {
        return DefaultLabelAdapter.create(edu.umn.biomedicus.uima.type1_5.NormIndex.class,
                (normIndex, annotation) -> {
                    annotation.setIndex(normIndex.term().indexedTerm());
                },
                annotation -> new NormIndex(new IndexedTerm(annotation.getIndex()))
        );
    }
}
