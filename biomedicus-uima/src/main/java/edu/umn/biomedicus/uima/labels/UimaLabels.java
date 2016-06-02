package edu.umn.biomedicus.uima.labels;

import edu.umn.biomedicus.common.labels.AbstractLabels;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.SpanLike;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;

final class UimaLabels<T> extends AbstractLabels<T> {
    private final JCas jCas;
    private final LabelAdapter<T> labelAdapter;
    private final Iterable<Label<T>> iterable;

    private UimaLabels(JCas jCas, LabelAdapter<T> labelAdapter, Iterable<Label<T>> iterable) {
        this.jCas = jCas;
        this.labelAdapter = labelAdapter;
        this.iterable = iterable;
    }

    UimaLabels(JCas jCas, LabelAdapter<T> labelAdapter) {
        this.jCas = jCas;
        this.labelAdapter = labelAdapter;
        AnnotationIndex<? extends Annotation> annotationIndex = jCas.getAnnotationIndex(labelAdapter.getjCasClass());
        iterable = () -> new FSIteratorAdapter<>(annotationIndex, labelAdapter::adaptAnnotation);
    }

    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        AnnotationIndex<? extends Annotation> annotationIndex = jCas.getAnnotationIndex(labelAdapter.getjCasClass());
        Annotation bound = new Annotation(jCas, spanLike.getBegin() - 1, spanLike.getEnd() + 1);
        Iterable<Label<T>> iterable = () -> FSIteratorAdapter.coveredIteratorAdapter(annotationIndex, bound,
                labelAdapter::adaptAnnotation);
        return new UimaLabels<>(jCas, labelAdapter, iterable).filter(spanLike::contains);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return iterable.iterator();
    }
}
