package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

public interface Labeler<T> {
    void addLabel(Span span, T label) throws BiomedicusException;

    void addLabel(Label<T> label) throws BiomedicusException;
}
