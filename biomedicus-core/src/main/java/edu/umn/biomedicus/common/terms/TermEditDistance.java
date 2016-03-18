package edu.umn.biomedicus.common.terms;

import edu.umn.biomedicus.common.collect.Metric;

/**
 *
 */
public class TermEditDistance implements Metric<IndexedTerm> {

    private final TermIndex termIndex;

    private final Metric<String> editDistance;

    public TermEditDistance(TermIndex termIndex, Metric<String> editDistance) {
        this.termIndex = termIndex;
        this.editDistance = editDistance;
    }

    @Override
    public int compute(IndexedTerm first, IndexedTerm second) {
        String firstString = termIndex.getString(first);
        String secondString = termIndex.getString(second);
        return editDistance.compute(firstString, secondString);
    }
}
