package edu.umn.biomedicus.vocabulary;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.terms.TermIndex;

/**
 *
 */
@ProvidedBy(VocabularyLoader.class)
public class Vocabulary {

    private final TermIndex wordIndex;

    Vocabulary(TermIndex wordIndex) {
        this.wordIndex = wordIndex;
    }

    public TermIndex wordIndex() {
        return wordIndex;
    }
}
