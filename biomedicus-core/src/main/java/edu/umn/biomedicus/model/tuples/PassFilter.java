package edu.umn.biomedicus.model.tuples;

/**
 *
 */
public class PassFilter implements WordCapFilter {
    @Override
    public boolean test(WordCap wordCap) {
        return true;
    }
}
