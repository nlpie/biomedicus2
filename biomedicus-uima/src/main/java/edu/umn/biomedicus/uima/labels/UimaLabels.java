package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.SpanLike;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;


@DocumentScoped
public final class UimaLabels<T> implements Labels<T> {
    private final JCas jCas;
    private final LabelAdapter<T> labelAdapter;
    private final Iterable<Label<T>> iterable;

    public UimaLabels(JCas jCas, LabelAdapter<T> labelAdapter, Iterable<Label<T>> iterable) {
        this.jCas = jCas;
        this.labelAdapter = labelAdapter;
        this.iterable = iterable;
    }

    @Inject
    public UimaLabels(JCas jCas, LabelAdapter<T> labelAdapter) {
        this.jCas = jCas;
        this.labelAdapter = labelAdapter;
        AnnotationIndex<? extends Annotation> annotationIndex = jCas.getAnnotationIndex(labelAdapter.getjCasClass());
        iterable = () -> new FSIteratorAdapter<>(annotationIndex, labelAdapter::adaptAnnotation);
    }

    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        AnnotationIndex<? extends Annotation> annotationIndex = jCas.getAnnotationIndex(labelAdapter.getjCasClass());
        Annotation bound = new Annotation(jCas, spanLike.getBegin(), spanLike.getEnd() + 1);
        Iterable<Label<T>> iterable = () -> FSIteratorAdapter.coveredIteratorAdapter(annotationIndex, bound,
                labelAdapter::adaptAnnotation);
        return new UimaLabels<>(jCas, labelAdapter, iterable).filter(spanLike::contains);
    }

    @Override
    public Labels<T> withSpan(SpanLike spanLike) {
        return insideSpan(spanLike).filter(spanLike::spanEquals);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return iterable.iterator();
    }
}
