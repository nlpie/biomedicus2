package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.jcas.JCas;

public final class UimaLabeler<T> implements Labeler<T> {

    private final LabelAdapter<T> labelAdapter;

    private final JCas jCas;

    @Inject
    public UimaLabeler(LabelAdapter<T> labelAdapter, JCas jCas) {
        this.labelAdapter = labelAdapter;
        this.jCas = jCas;
    }

    @Override
    public ValueLabeler value(T value) {
        return new ValueLabeler() {
            @Override
            public void label(int begin, int end) throws BiomedicusException {
                label(Span.spanning(begin, end));
            }

            @Override
            public void label(SpanLike spanLike) throws BiomedicusException {
                labelAdapter.createLabel(jCas, new Label<>(spanLike.toSpan(), value));
            }
        };
    }
}
