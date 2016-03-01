package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.model.text.Token;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * A vector space used to calculate word vectors from context
 * Used by the AcronymExpander
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class VectorSpaceDouble implements Serializable {

    // Should the IDF be squared, to effectively apply it to both test and train vectors (or raised to another power)?
    private final double idfPower;
    // Distance to use for weighting function
    private final double maxDist;
    // The actual size of the window; past threshWeight, we won't even consider words
    private final double windowSize;
    private Map<String, Integer> dictionary = new HashMap<>();
    // A count of how many "documents" (training examples) contain each term
    // Maps words (in their integer form, accessible through dictionary) to counts
    private Map<Integer, Integer> documentsPerTerm = new HashMap<>();
    // Number of "documents" (contexts) seen in training
    private int totalDocs = 0;
    // A log-transformed version of documentsPerTerm
    private DoubleVector idf;
    // This will be set to false when calculating the idf, and terms will no longer be added to IDF counts
    private boolean training = true;
    // How quickly the sigmoid falls off. More of an idiosyncratic steepness parameter than a slope
    private double slope = 0.3;
    // Default weighting function is sigmoid that decreases with distance (to 0.5 at maxDist)
    // Need to cast to Serializable to save it
    private BiFunction<Integer, Double, Double> distWeight = (BiFunction<Integer, Double, Double> & Serializable) (dist, maxDist) -> 1.0 / (1.0 + Math.exp(slope * (Math.abs(dist) - maxDist)));

    /**
     * Default constructor, uses maximum distance of 9.
     */
    public VectorSpaceDouble() {
        this(9);
    }

    /**
     * Constructor taking maximum distance as an argument.
     *
     * @param maxDist maximum distance.
     */
    public VectorSpaceDouble(double maxDist) {
        this.maxDist = maxDist;
        idfPower = 1;
        double threshWeight = 0.25;
        // algebra tells us this is the window size to use
        windowSize = Math.log(1.0 / threshWeight - 1) / slope + maxDist;
    }

    /**
     * Check if a string is alphanumeric (with some symbols allowed)
     *
     * @param s string to check
     * @return whether it is
     */
    private static boolean isAlphanumeric(String s) {
        if (s.matches("[a-zA-Z0-9.&_]*")) return true;
        return false;
    }

    public int numWords() {
        return dictionary.size();
    }

    public DoubleVector getIdf() {
        return idf;
    }

    /**
     * This needs to be called after all training vectors have been passed.
     * It sets up the IDF for each term and will save cycles at test time by stopping counting for the IDF
     */
    public void finishTraining() {
        Map<Integer, Double> idf = new HashMap<>();
        // Add 1 to denominator in case there are zero-counts, and to numerator in case there are 'all'-counts
        for (Map.Entry<Integer, Integer> e : documentsPerTerm.entrySet()) {
            double logged = Math.pow(Math.log((1 + (double) totalDocs) / (e.getValue())), idfPower);
            idf.put(e.getKey(), logged);
        }
        this.idf = new WordVectorDouble(idf);
        training = false;
    }

    /** Generate a WordVectorSpaceFloat from a list of Tokens
     * The Token of interest should also be passed so we know positions for weighting
     */
    /**
     * Generate a WordVectorDouble from a list of Tokens
     *
     * @param context         A list of Tokens taken from the Document that the word of interest appears in
     * @param tokenOfInterest The token that we want to calculate a vector for
     * @return The calculated vector
     */
    public WordVectorDouble vectorize(List<Token> context, Token tokenOfInterest) {

        assert context.contains(tokenOfInterest);

        Map<Integer, Double> wordVector = new HashMap<>();

        // Contains a list of words in the given tokens (standard forms, and filtering out non-alphanumeric tokens)
        List<Integer> wordIntList = new ArrayList<>();

        // If we're still determining IDF of tokens, we'll use this Set at the end to update those counts
        Set<Integer> wordIntSetForIdf = new HashSet<>();

        // Index of the center token in our list of words
        int centerWord = 0;
        // To determine our position in the word list. Useful when calculating distance from center
        int i = 0;

        for (Token token : context) {

            // Determine if we've hit the token of interest yet
            if (centerWord == 0 && token == tokenOfInterest) {
                centerWord = i;
            }

            // Generate a list of words, if deemed acceptable words, whose values in the vector will be updated
            String word = standardForm(token);
            if (isAlphanumeric(word) || token == tokenOfInterest) {
                int wordInt = dictionary.getOrDefault(word, -1);
                if (training) {
                    dictionary.putIfAbsent(word, dictionary.size());
                    wordInt = dictionary.get(word);
                }
                wordIntList.add(wordInt);
                i++;
                if (training) {
                    wordIntSetForIdf.add(wordInt);
                }
            }
        }
        // Array of integers that correspond to the position relative to tokenOfInterest of each word in the wordList
        int[] position = IntStream.range(-centerWord, wordIntList.size() - centerWord).toArray();
        i = 0;
        for (int wordInt : wordIntList) {
            if (Math.abs(position[i]) <= windowSize && position[i] != 0) {
                double thisCount = distWeight.apply(position[i], maxDist);
                double oldWordScore = 0;
                // Don't add the center token (the one at position 0); that's the term of interest
                if (position[i] != 0) {
                    if (wordVector.containsKey(wordInt)) {
                        oldWordScore = wordVector.get(wordInt);
                    }
                    wordVector.put(wordInt, oldWordScore + thisCount);
                }
            }
            i++;
        }

        // Update the counts needed for calculating an IDF if we're still in the training phase
        if (training) {
            for (int wordInt : wordIntSetForIdf) {
                documentsPerTerm.putIfAbsent(wordInt, 0);
                documentsPerTerm.put(wordInt, documentsPerTerm.get(wordInt) + 1);
            }
            totalDocs++;
        }
        return new WordVectorDouble(wordVector);
    }

    /**
     * Return a stemmed, case-insensitive, and de-numeralized version of the string
     *
     * @param t a token
     * @return its flattened form
     */
    private String standardForm(Token t) {
        String form = t.getNormalForm();
        if (form == null)
            form = t.getText();
        return AcronymModel.standardFormString(form).toLowerCase();
    }

    /**
     * For de-identification purposes: remove a single word from the dictionary
     *
     * @param word a string of the word to be removed
     * @return the integer index of the word removed
     */
    public int removeWord(String word) {
        System.out.println(word);
        return dictionary.remove(AcronymModel.standardFormString(word).toLowerCase());
    }

    /**
     * For de-identification: remove all words except those in a given set
     *
     * @param wordsToKeep the set of words (in String format) to keep
     * @return a Set of integers corresponding to the words removed
     */
    public Set<Integer> removeWordsExcept(Set<String> wordsToKeep) {
        System.out.println(dictionary.size());
        Set<Integer> indicesRemoved = new HashSet<>();
        Set<String> wordsInDictionary = new HashSet<>(dictionary.keySet());
        for (String word : wordsInDictionary) {
            word = AcronymModel.standardFormString(word).toLowerCase();
            if (!wordsToKeep.contains(word)) {
                indicesRemoved.add(removeWord(word));
            }
        }
        System.out.println(indicesRemoved.size());
        System.out.println(dictionary.size());
        return indicesRemoved;
    }

}
