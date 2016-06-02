package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.semantics.Concept;

import java.util.List;

public final class DictionaryTerm {
    private final List<Concept> concepts;

    public DictionaryTerm(List<Concept> concepts) {
        this.concepts = concepts;
    }

    public List<Concept> getConcepts() {
        return concepts;
    }
}
