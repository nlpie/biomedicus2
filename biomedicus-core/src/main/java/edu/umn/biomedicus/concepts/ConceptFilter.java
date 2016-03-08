package edu.umn.biomedicus.concepts;

import java.util.Set;

/**
 *
 */
public class ConceptFilter {
    private final Set<TUI> blacklistedTuis;

    private final Set<CUI> blacklistedCuis;

    private final Set<String> blacklistedWords;

    private final Set<String> blacklistedNorms;

    public ConceptFilter(Set<TUI> blacklistedTuis,
                         Set<CUI> blacklistedCuis,
                         Set<String> blacklistedWords,
                         Set<String> blacklistedNorms) {
        this.blacklistedTuis = blacklistedTuis;
        this.blacklistedCuis = blacklistedCuis;
        this.blacklistedWords = blacklistedWords;
        this.blacklistedNorms = blacklistedNorms;
    }

    boolean wordIsBlacklisted(String word) {
        return blacklistedWords.contains(word);
    }
}
