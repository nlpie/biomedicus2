package edu.umn.biomedicus.model.tuples;

/**
 *
 */
public class IsCapitalizedFilter implements WordCapFilter {
    @Override
    public boolean test(WordCap wordCap) {
        return wordCap.isCapitalized();
    }
}
