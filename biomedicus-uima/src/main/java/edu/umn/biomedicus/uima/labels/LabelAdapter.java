package edu.umn.biomedicus.uima.labels;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public interface LabelAdapter<T> {
    Class<? extends Annotation> getjCasClass();

    void createLabel(JCas jCas, Label<T> label) throws BiomedicusException;

    Label<T> adaptAnnotation(Annotation annotation);
}
