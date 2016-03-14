package edu.umn.biomedicus.vocabulary;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.terms.TermIndex;

/**
 *
 */
@ProvidedBy(VocabularyLoader.class)
public class Vocabulary {
    private final TermIndex wordIndex;

    private final TermIndex normIndex;

    public Vocabulary(TermIndex wordIndex, TermIndex normIndex) {
        this.wordIndex = wordIndex;
        this.normIndex = normIndex;
    }

    public TermIndex wordIndex() {
        return wordIndex;
    }

    public TermIndex normIndex() {
        return normIndex;
    }
}
