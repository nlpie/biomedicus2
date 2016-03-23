package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.common.collect.IndexMap;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 */
public class OrthographicAcronymModelTrainer {
    public static void main(String[] args) {
        Path abbrevsPath = Paths.get(args[0]);
        Path longformsPath = Paths.get(args[1]);
        OrthographicAcronymModelTrainer orthographicAcronymModelTrainer = new OrthographicAcronymModelTrainer(true);
        orthographicAcronymModelTrainer.setAbbrevPath(abbrevsPath);
        orthographicAcronymModelTrainer.setLongformsPath(longformsPath);
        try {
            orthographicAcronymModelTrainer.trainTrigramModel();
            orthographicAcronymModelTrainer.write(Paths.get(args[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Amount of discounting to apply to trigram counts; this amount gets applied to the interpolated bi/unigram counts
    private static final double discounting = .9;

    private final boolean caseSensitive;

    private final transient IndexMap<Character> symbols;

    private final transient int symbolsCount;

    private final transient Set<Character> chars;

    private final double[][][] longformProbs;

    private final double[][][] abbrevProbs;

    /**
     * Use as a lookup to be sure that words are not abbreviations.
     * Makes the model significantly larger
     */
    private Set<String> longformsLower;

    private Path abbrevPath;

    private Path longformsPath;

    public OrthographicAcronymModelTrainer(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        symbols = caseSensitive ? OrthographicAcronymModel.CASE_SENS_SYMBOLS : OrthographicAcronymModel.CASE_INSENS_SYMBOLS;
        symbolsCount = symbols.size();
        chars = caseSensitive ? OrthographicAcronymModel.CASE_SENS_CHARS : OrthographicAcronymModel.CASE_INSENS_CHARS;
        longformProbs = new double[symbolsCount][symbolsCount][symbolsCount];
        abbrevProbs = new double[symbolsCount][symbolsCount][symbolsCount];
        longformsLower = new HashSet<>();
    }

    public void setAbbrevPath(Path abbrevPath) {
        this.abbrevPath = abbrevPath;
    }

    public void setLongformsPath(Path longformsPath) {
        this.longformsPath = longformsPath;
    }

    /**
     * Read in text files of longforms and abbreviations and build up sets to train the models
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void trainTrigramModel() throws IOException {
        // Read in the files containing all the longforms and abbreviations
        Set<String> abbrevs = Files.lines(abbrevPath).collect(Collectors.toSet());
        Set<String> longforms = Files.lines(longformsPath).collect(Collectors.toSet());
        longformsLower = longforms.stream().map(String::toLowerCase).collect(Collectors.toSet());

        wordsToLogProbs(longforms, longformProbs);
        wordsToLogProbs(abbrevs, abbrevProbs);
    }

    /**
     * For use in experiments. Probably not necessary otherwise
     *
     * @param words
     * @return
     */
    private void wordsToLogProbs(Set<String> words, double[][][] probs) {
        int[][][] counts = new int[symbolsCount][symbolsCount][symbolsCount];
        for (String word : words) {
            addTrigramsFromWord(word, counts);
        }
        for (int i = 0; i < symbolsCount; i++) {
            for (int j = 0; j < symbolsCount; j++) {
                for (int k = 0; k < symbolsCount; k++) {
                    probs[i][j][k] = (float) getTrigramLogProbability(i, j, k, counts);
                }
            }
        }
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
            thisChar = fixChar(word.charAt(i));

            counts[symbols.indexOf(minus2)][symbols.indexOf(minus1)][symbols.indexOf(thisChar)]++;

            minus2 = minus1;
            minus1 = thisChar;
        }
        counts[symbols.indexOf(minus1)][symbols.indexOf(thisChar)][symbols.indexOf('$')]++;
        counts[symbols.indexOf(thisChar)][symbols.indexOf('$')][symbols.indexOf('$')]++;
    }

    /**
     * Assures that a character matches a character known to the model
     *
     * @param c a character as it appears in a word
     * @return the character to use for N-grams
     */
    private char fixChar(char c) {
        if (!caseSensitive) {
            c = Character.toLowerCase(c);
        }
        if (Character.isDigit(c)) {
            c = '0';
        } else if (!chars.contains(c)) {
            c = '?';
        }
        return c;
    }

    private void write(Path out) throws IOException {
        Yaml yaml = new Yaml();

        Map<String, Object> serObj = new TreeMap<>();
        serObj.put("abbrevProbs", collapseProbs(abbrevProbs));
        serObj.put("longformProbs", collapseProbs(longformProbs));
        serObj.put("longformsLower", longformsLower.stream().collect(Collectors.toList()));
        serObj.put("caseSensitive", caseSensitive);

        yaml.dump(serObj, Files.newBufferedWriter(out));
    }

    private Map<String, Double> collapseProbs(double[][][] probs) {
        Map<String, Double> collapsedAbbrevProbs = new TreeMap<>();
        for (int i = 0; i < probs.length; i++) {
            for (int j = 0; j < probs[i].length; j++) {
                for (int k = 0; k < probs[i][j].length; k++) {
                    double prob = probs[i][j][k];
                    if (prob != 0.0) {
                        collapsedAbbrevProbs.put("" + symbols.forIndex(i) + symbols.forIndex(j) + symbols.forIndex(k), prob);
                    }
                }
            }
        }
        return collapsedAbbrevProbs;
    }
}
