package edu.umn.biomedicus.model.semantics;

import edu.umn.biomedicus.model.text.Span;

/**
 * Created by benknoll on 9/9/15.
 */
public interface SubstanceUsageBuilder {
    void addAmount(Span span);

    void addFrequency(Span span);

    void addType(Span span);

    void addStatus(Span span);

    void addTemporal(Span span);

    void addMethod(Span span);

    void build();
}
