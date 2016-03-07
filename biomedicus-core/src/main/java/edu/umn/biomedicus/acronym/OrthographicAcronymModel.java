package edu.umn.biomedicus.acronym;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.vocabulary.CharacterSet;
import edu.umn.biomedicus.common.vocabulary.MappedCharacterSet;

import java.io.Serializable;
import java.util.Set;

/**
 * Will use orthographic rules to determine if tokens not known to be abbreviations are abbreviations
 *
 * @author Greg Finley
 */
@ProvidedBy(OrthographicAcronymModelLoader.class)
public class OrthographicAcronymModel implements Serializable {

    public static final CharacterSet CASE_SENS_SYMBOLS = MappedCharacterSet.builder()
            .addAll("abcdefghijklmnopqrstuvwxyz.-ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            .addAll("0?^$")
            .build();

    public static final CharacterSet CASE_SENS_CHARS = CASE_SENS_SYMBOLS.maskCharacters("0?^$");

    public static final CharacterSet CASE_INSENS_SYMBOLS = MappedCharacterSet.builder()
            .addAll("abcdefghijklmnopqrstuvwxyz.-")
            .addAll("0?^$")
            .build();

    public static final CharacterSet CASE_INSENS_CHARS = CASE_INSENS_SYMBOLS.maskCharacters("0?^$");

    // Log probabilities that certain character trigrams are an abbreviation or a longform
    private final double[][][] abbrevProbs;

    private final double[][][] longformProbs;

    private final boolean caseSensitive;

    private final Set<String> longformsLower;

    private final transient CharacterSet symbols;

    private final transient CharacterSet chars;

    public OrthographicAcronymModel(double[][][] abbrevProbs, double[][][] longformProbs, boolean caseSensitive, Set<String> longformsLower) {
        this.abbrevProbs = abbrevProbs;
        this.longformProbs = longformProbs;
        this.caseSensitive = caseSensitive;
        this.longformsLower = longformsLower;
        symbols = caseSensitive ? CASE_SENS_SYMBOLS : CASE_INSENS_SYMBOLS;
        chars = caseSensitive ? CASE_SENS_CHARS : CASE_INSENS_CHARS;
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
        if (longformsLower != null && (longformsLower.contains(wordLower) || (normalForm != null && longformsLower.contains(normalForm.toLowerCase())))) {
            return false;
        }

        // If not, use basic intuitive rules (all vowels or consonants, etc.)

        if (wordRaw.length() < 2) {
            return false;
        }
        // No letters? Then it's probably punctuation or a numeral
        if (wordLower.matches("[^a-z]*")) {
            return false;
        }
        // No vowels, or only vowels? Then it's probably an abbreviation
        if (wordLower.matches("[^bcdfghjklmnpqrstvwxz]*")) {
            return true;
        }
        if (wordLower.matches("[^aeiouy]*")) {
            return true;
        }

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
        return !(abbrevProbs == null || longformProbs == null) && getWordLikelihood(form, abbrevProbs) > getWordLikelihood(form, longformProbs);
    }

    // make private after testing

    /**
     * Calculates the log likelihood of a word according to a model
     *
     * @param word  the word
     * @param probs a 3-d array of log probabilities
     * @return the log likelihood of this word
     */
    public double getWordLikelihood(String word, double[][][] probs) {
        char minus2 = '^';
        char minus1 = '^';
        char thisChar = '^';
        double logProb = 0;

        for (int i = 0; i < word.length(); i++) {
            thisChar = fixChar(word.charAt(i));

            logProb += probs[symbols.indexOf(minus2)][symbols.indexOf(minus1)][symbols.indexOf(thisChar)];

            minus2 = minus1;
            minus1 = thisChar;
        }

        logProb += probs[symbols.indexOf(minus1)][symbols.indexOf(thisChar)][symbols.indexOf('$')];
        logProb += probs[symbols.indexOf(thisChar)][symbols.indexOf('$')][symbols.indexOf('$')];

        return logProb;
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
        } else if (chars.indexOf(c) == -1) {
            c = '?';
        }
        return c;
    }
}
