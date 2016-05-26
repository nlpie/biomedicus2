package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.Span;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Optional;
import java.util.function.Function;


@DocumentScoped
public final class UimaLabels<T> implements Labels<T> {
    private final JCas jCas;

    private final LabelAdapter<T> labelAdapter;

    @Inject
    public UimaLabels(JCas jCas, LabelAdapter<T> labelAdapter) {
        this.jCas = jCas;
        this.labelAdapter = labelAdapter;
    }

    @Override
    public Iterable<Label<T>> all() {
        AnnotationIndex<? extends Annotation> annotationIndex = jCas.getAnnotationIndex(labelAdapter.getjCasClass());
        Function<Annotation, Label<T>> adapter = labelAdapter::adaptAnnotation;
        return () -> new FSIteratorAdapter<>(annotationIndex, adapter);
    }

    @Override
    public Iterable<Label<T>> inSpan(Span span) {
        return null;
    }

    @Override
    public Iterable<Label<T>> matchingSpan(Span span) {
        return null;
    }

    @Override
    public Optional<Label<T>> oneMatchingSpan(Span span) {
        return null;
    }
}
