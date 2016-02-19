package edu.umn.biomedicus.model.tuples;

/**
 *
 */
public class LowercaseAdapter implements WordCapAdapter {
    @Override
    public WordCap apply(WordCap wordCap) {
        return wordCap.lowercase();
    }
}
