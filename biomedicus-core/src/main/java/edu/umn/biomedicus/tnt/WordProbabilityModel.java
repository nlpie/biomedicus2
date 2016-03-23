package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.tuples.WordPosCap;

import java.util.Set;

/**
 *
 */
public interface WordProbabilityModel {


    /**
     * Convenience method for #logProbabilityOfWord(edu.umn.biomedicus.syntax.tnt.models.WordPosCap). Constructs a new
     * {@link WordPosCap} from the arguments.
     *
     * @param candidate     the conditional PartOfSpeech
     * @return a negative double representing the log10 probability of the word
     */
    double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap);

    /**
     * Returns the potential part of speech candidates for a given word
     *
     * @return a set of {@link PartOfSpeech} enum values
     */
    Set<PartOfSpeech> getCandidates(WordCap wordCap);

    /**
     * Given a word, returns if this model can account for its probability.
     *
     * @return true if this model can provide a probability for the word, false otherwise
     */
    boolean isKnown(WordCap wordCap);

    void reduce();
}
