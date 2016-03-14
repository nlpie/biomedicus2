package edu.umn.biomedicus.common.terms;

/**
 *
 */
public class DAWGTermIndex extends AbstractTermIndex {
    private final DirectedAcyclicWordGraph directedAcyclicWordGraph;

    public DAWGTermIndex(DirectedAcyclicWordGraph directedAcyclicWordGraph) {
        this.directedAcyclicWordGraph = directedAcyclicWordGraph;
    }

    @Override
    String getTerm(int termIdentifier) {
        return directedAcyclicWordGraph.forIndex(termIdentifier);
    }

    @Override
    int internalizeInt(CharSequence term) {
        return directedAcyclicWordGraph.indexOf(term);
    }

    @Override
    public String getString(IndexedTerm indexedTerm) {
        return getTerm(indexedTerm.termIdentifier());
    }

    @Override
    public IndexedTerm lookup(CharSequence term) {
        return new IndexedTerm(internalizeInt(term));
    }
}
