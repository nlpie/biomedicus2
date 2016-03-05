package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.model.text.Token;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * An implementation of an acronym model that uses word vectors and a cosine distance metric
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class AcronymVectorModel implements AcronymModel {

    public static final String UNK = "(unknown)";

    /**
     *  A vector space with a built dictionary to use at test time
     */
    private VectorSpaceDouble vectorSpaceDouble;

    /**
     *  A map between acronyms and all their possible long forms
     */
    private Map<String, String[]> expansionMap;

    /**
     * Maps long forms to their trained word vectors
     */
    private Map<String, DoubleVector> senseMap;

    /**
     *  The alignment model will guess an acronym's full form based on its alignment if we don't know what it is.
     */
    private AlignmentModel alignmentModel;

    public AcronymVectorModel() {

    }

    /**
     * Constructor. Needs several things already made:
     *
     * @param vectorSpaceDouble the vector space (most importantly dictionary) used to build context vectors
     * @param senseMap          which maps between senses and their context vectors
     * @param expansionMap      which maps between acronym Strings and Lists of their possible senses
     * @param alignmentModel    a model used for alignment of unknown acronyms
     */
    public AcronymVectorModel(VectorSpaceDouble vectorSpaceDouble, Map<String, DoubleVector> senseMap, Map<String, String[]> expansionMap, @Nullable AlignmentModel alignmentModel) {
        this.expansionMap = expansionMap;
        this.senseMap = senseMap;
        this.vectorSpaceDouble = vectorSpaceDouble;
        this.alignmentModel = alignmentModel;
    }

    /**
     * Constructor without providing an AlignmentModel
     */
    public AcronymVectorModel(VectorSpaceDouble vectorSpaceDouble, Map<String, DoubleVector> senseMap, Map<String, String[]> expansionMap) {
        this(vectorSpaceDouble, senseMap, expansionMap, null);
    }

    public VectorSpaceDouble getVectorSpaceDouble() {
        return vectorSpaceDouble;
    }

    public void setVectorSpaceDouble(VectorSpaceDouble vectorSpaceDouble) {
        this.vectorSpaceDouble = vectorSpaceDouble;
    }

    public Map<String, String[]> getExpansionMap() {
        return expansionMap;
    }

    public void setExpansionMap(Map<String, String[]> expansionMap) {
        this.expansionMap = expansionMap;
    }

    public Map<String, DoubleVector> getSenseMap() {
        return senseMap;
    }

    public void setSenseMap(Map<String, DoubleVector> senseMap) {
        this.senseMap = senseMap;
    }

    public AlignmentModel getAlignmentModel() {
        return alignmentModel;
    }

    public void setAlignmentModel(AlignmentModel alignmentModel) {
        this.alignmentModel = alignmentModel;
    }

    /**
     * Will return a list of the possible senses for this acronym
     *
     * @param token a Token
     * @return a List of Strings of all possible senses
     */
    public String[] getExpansions(Token token) {
        String acronym = AcronymUtilities.standardForm(token);
        if (expansionMap.containsKey(acronym))
            return expansionMap.get(acronym);
        return new String[0];
    }

    /**
     * Does the model know about this acronym?
     *
     * @param token
     * @return
     */
    public boolean hasAcronym(Token token) {
        String acronym = AcronymUtilities.standardForm(token);
        if (expansionMap.containsKey(acronym)) {
            return true;
        }
        return false;
    }

    /**
     * Will return the model's best guess for the sense of this acronym
     *
     * @param context a list of tokens providing context for this acronym
     * @param token   the token of the acronym itself
     * @return
     */
    @Override
    public String findBestSense(List<Token> context, Token token) {

        // String to assign to unknown acronyms

        String acronym = AcronymUtilities.standardForm(token);

        // If the model doesn't contain this acronym, make sure it doesn't contain an upper-case version of it

        String[] senses = expansionMap.get(acronym);
        if (senses == null) {
            senses = expansionMap.get(acronym.toUpperCase());
        }
        if (senses == null && alignmentModel != null) {
            senses = alignmentModel.findBestLongforms(acronym);
        }
        if (senses == null || senses.length == 0) {
            return UNK;
        }

        // If the acronym is unambiguous, our work is done
        if (senses.length == 1) {
            return senses[0];
        }


        List<String> usableSenses = new ArrayList<>();
        // Be sure that there even are disambiguation vectors for senses
        for (String sense : senses) {
            if (senseMap.containsKey(sense)) {
                usableSenses.add(sense);
            }
        }

        // If no senses good for disambiguation were found, try the upper-case version
        if (usableSenses.size() == 0 && expansionMap.containsKey(acronym.toUpperCase())) {
            for (String sense : senses) {
                if (senseMap.containsKey(sense)) {
                    usableSenses.add(sense);
                }
            }
        }

        // Should this just guess the first sense instead?
        if (usableSenses.size() == 0) {
            return UNK;
        }

        double best = -Double.MAX_VALUE;
        String winner = UNK;

        DoubleVector vector = vectorSpaceDouble.vectorize(context, token);
        vector.multiply(vectorSpaceDouble.getIdf());

        // Loop through all possible senses for this acronym
        for (String sense : usableSenses) {
            DoubleVector compVec = senseMap.get(sense);
            double score = vector.dot(compVec);
            if (score > best) {
                best = score;
                winner = sense;
            }
        }
        return winner;
    }

    /**
     * Remove a single word from the model
     *
     * @param word the word to remove
     */
    public void removeWord(String word) {
        vectorSpaceDouble.removeWord(word);
    }

    /**
     * Remove all words from the model except those given
     *
     * @param wordsToRemove the set of words to keep
     */
    public void removeWordsExcept(Set<String> wordsToRemove) {
        vectorSpaceDouble.removeWordsExcept(wordsToRemove);
    }
}