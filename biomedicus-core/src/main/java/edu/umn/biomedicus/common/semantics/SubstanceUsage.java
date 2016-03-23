package edu.umn.biomedicus.common.semantics;

import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.TextSpan;

/**
 * Created by benknoll on 9/3/15.
 */
public interface SubstanceUsage extends TextSpan {
    Sentence getSentence();

    SubstanceUsageType getSubstanceUsageType();

    Iterable<TextSpan> getAmounts();

    Iterable<TextSpan> getFrequencies();

    Iterable<TextSpan> getTypes();

    Iterable<TextSpan> getStatuses();

    Iterable<TextSpan> getTemporal();

    Iterable<TextSpan> getMethods();
}
