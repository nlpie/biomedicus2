package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.model.text.Token;

import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * An implementation of an acronym model that uses word vectors and a cosine distance metric
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class AcronymVectorModel implements Serializable, AcronymModel {

    // A vector space with a built dictionary to use at test time
    private final VectorSpaceDouble vectorSpaceDouble;

    // A map between acronyms and all their possible long forms
    private final Map<String, List<String>> expansionMap;

    // Maps long forms to their trained word vectors
    private final Map<String, DoubleVector> senseMap;

    // The alignment model will guess an acronym's full form based on its alignment if we don't know what it is
    private final AlignmentModel alignmentModel;

    /**
     * Constructor. Needs several things already made:
     *
     * @param vectorSpaceDouble the vector space (most importantly dictionary) used to build context vectors
     * @param senseMap          which maps between senses and their context vectors
     * @param expansionMap      which maps between acronym Strings and Lists of their possible senses
     * @param alignmentModel    a model used for alignment of unknown acronyms
     */
    public AcronymVectorModel(VectorSpaceDouble vectorSpaceDouble, Map<String, DoubleVector> senseMap, Map<String, List<String>> expansionMap, @Nullable AlignmentModel alignmentModel) {
        this.expansionMap = expansionMap;
        this.senseMap = senseMap;
        this.vectorSpaceDouble = vectorSpaceDouble;
        this.alignmentModel = alignmentModel;
    }

    /**
     * Constructor without providing an AlignmentModel
     */
    public AcronymVectorModel(VectorSpaceDouble vectorSpaceDouble, Map<String, DoubleVector> senseMap, Map<String, List<String>> expansionMap) {
        this(vectorSpaceDouble, senseMap, expansionMap, null);
    }

    /**
     * Will return a list of the possible senses for this acronym
     *
     * @param token a Token
     * @return a List of Strings of all possible senses
     */
    public List<String> getExpansions(Token token) {
        String acronym = AcronymModel.standardForm(token);
        if (expansionMap.containsKey(acronym))
            return new ArrayList<>(expansionMap.get(acronym));
        return new ArrayList<>();
    }

    /**
     * Does the model know about this acronym?
     *
     * @param token
     * @return
     */
    public boolean hasAcronym(Token token) {
        String acronym = AcronymModel.standardForm(token);
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
        final String UNK = "(unknown)";

        Set<String> allSenses;
        Set<String> usableSenses = new HashSet<>();

        String acronym = AcronymModel.standardForm(token);

        // If the model doesn't contain this acronym, make sure it doesn't contain an upper-case version of it
        if (!expansionMap.containsKey(acronym)) {
            if (!expansionMap.containsKey(acronym.toUpperCase())) {
                // if it still doesn't, then try to expand using the alignment model
                if (alignmentModel != null) {
                    allSenses = new HashSet<>(alignmentModel.findBestLongforms(acronym));
                } else {
                    return UNK;
                }
            }
            // If it contains only the upper-case form, just set it to that
            else {
                acronym = acronym.toUpperCase();
                allSenses = new HashSet<>(expansionMap.get(acronym));
            }
        } else {
            allSenses = new HashSet<>(expansionMap.get(acronym));
        }

        // If the acronym is unambiguous, our work is done
        if (allSenses.size() == 1) {
            return (String) allSenses.toArray()[0];
        }

        // Be sure that there even are disambiguation vectors for senses
        for (String sense : allSenses) {
            if (senseMap.containsKey(sense))
                usableSenses.add(sense);
        }
        // If no senses good for disambiguation were found, try the upper-case version
        if (usableSenses.size() == 0 && expansionMap.containsKey(acronym.toUpperCase())) {
            for (String sense : allSenses) {
                if (senseMap.containsKey(sense))
                    usableSenses.add(sense);
            }
        }

        // Should this just guess the first sense instead?
        if (usableSenses.size() == 0)
            return UNK;

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
     * Write this object to a file
     *
     * @param filename the name of the output file (*.ser)
     * @throws IOException
     */
    public void serialize(String filename) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);
        objectOutputStream.writeObject(this);
        gzipOutputStream.flush();
        gzipOutputStream.close();
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