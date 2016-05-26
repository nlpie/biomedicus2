package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.text.Span;
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
    public void addLabel(Span span, T label) throws BiomedicusException {
        addLabel(new Label<>(span, label));
    }

    @Override
    public void addLabel(Label<T> label) throws BiomedicusException {
        labelAdapter.createLabel(jCas, label);
    }
}
