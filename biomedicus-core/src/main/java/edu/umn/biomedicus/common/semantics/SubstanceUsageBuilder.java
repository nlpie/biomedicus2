package edu.umn.biomedicus.common.semantics;

import edu.umn.biomedicus.common.text.SpanLike;

/**
 * Created by benknoll on 9/9/15.
 */
public interface SubstanceUsageBuilder {
    void addAmount(SpanLike spanLike);

    void addFrequency(SpanLike spanLike);

    void addType(SpanLike spanLike);

    void addStatus(SpanLike spanLike);

    void addTemporal(SpanLike spanLike);

    void addMethod(SpanLike spanLike);

    void build();
}
