package edu.umn.biomedicus.model.tuples;

/**
 *
 */
public class LowercaseIgnoreCapitalizationAdapter implements WordCapAdapter {
    @Override
    public WordCap apply(WordCap wordCap) {
        return wordCap.lowercaseIgnoreCapitalization();
    }
}
