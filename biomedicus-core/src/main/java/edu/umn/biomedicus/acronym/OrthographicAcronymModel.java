package edu.umn.biomedicus.acronym;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.model.text.Token;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Will use orthographic rules to determine if tokens not known to be abbreviations are abbreviations
 *
 * @author Greg Finley
 */
@ProvidedBy(OrthographicAcronymModelLoader.class)
public class OrthographicAcronymModel implements Serializable {

    // Log probabilities that certain character trigrams are an abbreviation or a longform
    private float[][][] abbrevProbs;
    private float[][][] longformProbs;

    // List of characters that will be considered exactly as-is for the trigram model
    private String asis;
    private String symbols;
    // r is the number of symbols we end up with
    private int r;
    private Map<Character, Integer> charmap;

    // Amount of discounting to apply to trigram counts; this amount gets applied to the interpolated bi/unigram counts
    private double discounting = .9;

    private boolean caseSensitive;

    /**
     * Use as a lookup to be sure that words are not abbreviations.
     * Makes the model significantly larger
     */
    private Set<String> longformsLower;

    /**
     * Constructor, defaults to case sensitive (helps a lot for abbreviations in most cases; takes longer to train)
     */
    public OrthographicAcronymModel() {
        this(true);
    }

    /**
     * Constructor
     *
     * @param caseSensitive true if the model should be case sensitive
     */
    public OrthographicAcronymModel(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        if (caseSensitive) {
            asis = "abcdefghijklmnopqrstuvwxyz.-ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        } else {
            asis = "abcdefghijklmnopqrstuvwxyz.-";
        }
        // Add the other symbols to use:
        // 0 = digits
        // ? = other characters
        // ^ = beginning of word dummy token
        // $ = end of word dummy token
        symbols = asis + "0?^$";
        r = symbols.length();

        charmap = new HashMap<>();
        for (int i = 0; i < symbols.length(); i++) {
            char c = symbols.charAt(i);
            charmap.put(c, i);
        }
        abbrevProbs = new float[r][r][r];
        longformProbs = new float[r][r][r];
    }

    /**
     * Will load a serialized model, which might include some machine learning for identification
     *
     * @param filename
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static OrthographicAcronymModel loadFromSerialized(String filename) throws IOException, ClassNotFoundException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        ObjectInputStream ois = new ObjectInputStream(gzipInputStream);
        return (OrthographicAcronymModel) ois.readObject();
    }

    /**
     * Will determine whether this word is an abbreviation
     *
     * @param token the Token to check
     * @return true if it seems to be an abbreviation, false otherwise
     */
    public boolean seemsLikeAbbreviation(Token token) {

        String wordRaw = token.getText();
        String wordLower = wordRaw.toLowerCase();
        String normalForm = token.getNormalForm();

        // Check to see if it's a long form first
        // This is case-insensitive to curb overzealous tagging of abbreviations
        // Also check the normal form, if it exists, as affixed forms may not appear in the list of long forms
        if (longformsLower != null && (longformsLower.contains(wordLower) || (normalForm != null && longformsLower.contains(normalForm.toLowerCase()))))
            return false;

        // If not, use basic intuitive rules (all vowels or consonants, etc.)

        if (wordRaw.length() < 2) return false;
        // No letters? Then it's probably punctuation or a numeral
        if (wordLower.matches("[^a-z]*")) return false;
        // No vowels, or only vowels? Then it's probably an abbreviation
        if (wordLower.matches("[^bcdfghjklmnpqrstvwxz]*")) return true;
        if (wordLower.matches("[^aeiouy]*")) return true;

        // If the word form isn't suspicious by the intuitive rules, go to the trigram model
        return seemsLikeAbbrevByTrigram(wordRaw);
    }

    /**
     * Will determine if a character trigram model thinks this word is an abbreviation
     *
     * @param form the string form in question
     * @return true if abbreviation, false if not
     */
    public boolean seemsLikeAbbrevByTrigram(String form) {
        if (abbrevProbs == null || longformProbs == null) {
            return false;
        }
        if (getWordLikelihood(form, abbrevProbs) > getWordLikelihood(form, longformProbs)) {
            return true;
        }
        return false;
    }

    /**
     * Read in text files of longforms and abbreviations and build up sets to train the models
     *
     * @param abbrevFilename   the text file with abbreviations
     * @param longformFilename the text file with long forms (not necessarily matching those abbreviations)
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void trainTrigramModel(String abbrevFilename, String longformFilename) throws IOException {

        // Read in the files containing all the longforms and abbreviations
        Set<String> longforms = new HashSet<>();
        Set<String> abbrevs = new HashSet<>();

        longformsLower = new HashSet<>();

        InputStream inputStream = new FileInputStream(abbrevFilename);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine;
        while ((nextLine = fileReader.readLine()) != null) {
            abbrevs.add(nextLine);
        }
        inputStream = new FileInputStream(longformFilename);
        fileReader = new BufferedReader(new InputStreamReader(inputStream));
        while ((nextLine = fileReader.readLine()) != null) {
            longforms.add(nextLine);
            longformsLower.add(nextLine.toLowerCase());
        }

        trainTrigramModel(longforms, abbrevs);
    }

    /**
     * Will count up character trigrams and calculate log probabilities for abbrevs and longforms and save to a 3d array
     *
     * @param longforms the set of all strings corresponding to long forms
     * @param abbrevs   the set of abbreviations
     */
    public void trainTrigramModel(Set<String> abbrevs, Set<String> longforms) {
        int[][][] abbrevCounts = new int[r][r][r];
        int[][][] longformCounts = new int[r][r][r];

        longformProbs = wordsToLogProbs(longforms);
        abbrevProbs = wordsToLogProbs(abbrevs);

        // Count up trigrams for all
        for (String abbrev : abbrevs) {
            addTrigramsFromWord(abbrev, abbrevCounts);
        }
        for (String longform : longforms) {
            addTrigramsFromWord(longform, longformCounts);
        }

        // Calculate log probabilities for each trigram
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                for (int k = 0; k < r; k++) {
                    longformProbs[i][j][k] = (float) getTrigramLogProbability(i, j, k, longformCounts);
                    abbrevProbs[i][j][k] = (float) getTrigramLogProbability(i, j, k, abbrevCounts);
                }
            }
        }
    }

    // make private after testing

    /**
     * Counts character trigrams in a word and adds them to total counts of those trigrams
     *
     * @param word   the word to add trigrams from
     * @param counts the counts matrix (will be changed)
     */
    private void addTrigramsFromWord(String word, int[][][] counts) {
        char minus2 = '^';
        char minus1 = '^';
        char thisChar = '^';

        for (int i = 0; i < word.length(); i++) {
            thisChar = fixchar(word.charAt(i));

            counts[charmap.get(minus2)][charmap.get(minus1)][charmap.get(thisChar)]++;

            minus2 = minus1;
            minus1 = thisChar;
        }
        counts[symbols.indexOf(minus1)][symbols.indexOf(thisChar)][charmap.get('$')]++;
        counts[symbols.indexOf(thisChar)][charmap.get('$')][charmap.get('$')]++;
    }

    /**
     * Calculates the log likelihood of a word according to a model
     *
     * @param word  the word
     * @param probs a 3-d array of log probabilities
     * @return the log likelihood of this word
     */
    public double getWordLikelihood(String word, float[][][] probs) {
        char minus2 = '^';
        char minus1 = '^';
        char thisChar = '^';
        double logProb = 0;

        for (int i = 0; i < word.length(); i++) {
            thisChar = fixchar(word.charAt(i));

            logProb += probs[charmap.get(minus2)][charmap.get(minus1)][charmap.get(thisChar)];

            minus2 = minus1;
            minus1 = thisChar;
        }
        logProb += probs[symbols.indexOf(minus1)][symbols.indexOf(thisChar)][charmap.get('$')];
        logProb += probs[symbols.indexOf(thisChar)][charmap.get('$')][charmap.get('$')];

        return logProb;
    }

    /**
     * Assures that a character matches a character known to the model
     *
     * @param c a character as it appears in a word
     * @return the character to use for N-grams
     */
    private char fixchar(char c) {
        if (!caseSensitive) {
            c = Character.toLowerCase(c);
        }
        if (Character.isDigit(c)) {
            c = '0';
        } else if (!asis.contains("" + c)) {
            c = '?';
        }
        return c;
    }

    /**
     * For use in experiments. Probably not necessary otherwise
     *
     * @param words
     * @return
     */
    public float[][][] wordsToLogProbs(Set<String> words) {
        int[][][] counts = new int[r][r][r];
        float[][][] probs = new float[r][r][r];
        for (String word : words) {
            addTrigramsFromWord(word, counts);
        }
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                for (int k = 0; k < r; k++) {
                    probs[i][j][k] = (float) getTrigramLogProbability(i, j, k, counts);
                }
            }
        }

        return probs;
    }

    /**
     * Calculates the probability of the trigram (w2, w1, w) from a matrix of counts
     *
     * @param w2     an integer indicating the first word in the bigram
     * @param w1     an integer indicating the second word in the bigram
     * @param w      an integer indicating the third word in the bigram
     * @param counts a matrix of trigram counts
     * @return the log probability of this trigram, with discounting and interpolated backoff to bigram counts
     */
    private double getTrigramLogProbability(int w2, int w1, int w, int[][][] counts) {
        double prob = 0;

        int contextCount = tensorSum(counts[w2][w1]);
        if (contextCount == 0) {
            prob = getBigramProbability(w1, w, counts);
        } else {
            int triCount = counts[w2][w1][w];
            if (triCount > 0) {
                prob += (((double) triCount) - discounting) / contextCount;
            }
            double interpolationCoefficient = discounting * tensorSum(counts[w2][w1], true) / contextCount;
            prob += interpolationCoefficient * getBigramProbability(w1, w, counts);
        }

        // In case discounting or lack of data created weird situations
        if (prob <= 0) {
            prob = 1.0 / tensorSum(counts);
        }
        return Math.log(prob);
    }

    /**
     * Calculates the probability of the bigram (w1, w) from a matrix of counts
     *
     * @param w1     an integer indicating the first word in the bigram
     * @param w      an integer indicating the second word in the bigram
     * @param counts a matrix of trigram counts
     * @return the probability (not log) of this bigram, with discounting and  interpolated backoff to unigram counts
     */
    private double getBigramProbability(int w1, int w, int[][][] counts) {
        if (tensorSum(counts[w1]) == 0)
            return getUnigramProbability(w, counts);
        double prob = 0;
        int biCount = tensorSum(counts[w1][w]);
        if (biCount > 0) {
            prob += (((double) biCount) - discounting) / tensorSum(counts[w1]);
        }
        double unigramInterpCoefficient = discounting * tensorSum(counts[w1], true) / tensorSum(counts[w1]);
        prob += unigramInterpCoefficient * getUnigramProbability(w, counts);
        return prob;
    }

    private double getUnigramProbability(int w, int[][][] counts) {
        return ((double) tensorSum(counts[w])) / tensorSum(counts);
    }

    /**
     * Sum up a vector, matrix, or 3-d tensor of integers
     *
     * @param array    the tensor to be summed
     * @param nonzeros if true, only count the number of nonzero entries, not their sum
     * @return a sum
     */
    private int tensorSum(int[] array, boolean nonzeros) {
        int sum = 0;
        for (int i : array) {
            if (!nonzeros) {
                sum += i;
            } else if (i > 0) {
                sum++;
            }
        }
        return sum;
    }

    private int tensorSum(int[][] matrix, boolean nonzeros) {
        int sum = 0;
        for (int[] array : matrix) {
            sum += tensorSum(array, nonzeros);
        }
        return sum;
    }

    private int tensorSum(int[][][] tensor3, boolean nonzeros) {
        int sum = 0;
        for (int[][] matrix : tensor3) {
            sum += tensorSum(matrix, nonzeros);
        }
        return sum;
    }

    // If you don't want to provide the nonzeros argument
    private int tensorSum(int[] tensor) {
        return tensorSum(tensor, false);
    }

    private int tensorSum(int[][] tensor) {
        return tensorSum(tensor, false);
    }

    private int tensorSum(int[][][] tensor) {
        return tensorSum(tensor, false);
    }

    public void serialize(String filename) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);
        objectOutputStream.writeObject(this);
        gzipOutputStream.flush();
        gzipOutputStream.close();
    }
}
