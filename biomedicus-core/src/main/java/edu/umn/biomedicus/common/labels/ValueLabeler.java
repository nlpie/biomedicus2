package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.exc.BiomedicusException;

public interface ValueLabeler {
    void label(int begin, int end) throws BiomedicusException;

    void label(SpanLike spanLike) throws BiomedicusException;
}
