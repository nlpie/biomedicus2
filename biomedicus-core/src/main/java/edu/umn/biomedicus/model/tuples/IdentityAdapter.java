package edu.umn.biomedicus.model.tuples;

/**
 *
 */
public class IdentityAdapter implements WordCapAdapter {
    @Override
    public WordCap apply(WordCap wordCap) {
        return wordCap;
    }
}
