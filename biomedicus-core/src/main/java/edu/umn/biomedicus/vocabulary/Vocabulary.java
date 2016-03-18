package edu.umn.biomedicus.vocabulary;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.terms.TermIndex;

/**
 *
 */
@ProvidedBy(VocabularyLoader.class)
public class Vocabulary {
    private final TermIndex normIndex;

    private final TermIndex wordIndex;

    public Vocabulary(TermIndex wordIndex, TermIndex normIndex) {
        this.wordIndex = wordIndex;
        this.normIndex = normIndex;
    }

    public TermIndex normIndex() {
        return normIndex;
    }

    public TermIndex wordIndex() {
        return wordIndex;
    }
}
