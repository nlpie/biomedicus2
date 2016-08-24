/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.collect.HashIndexMap;
import edu.umn.biomedicus.common.collect.IndexMap;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Will use orthographic rules to determine if tokens not known to be abbreviations are abbreviations
 *
 * @author Greg Finley
 */
@ProvidedBy(OrthographicAcronymModel.Loader.class)
public class OrthographicAcronymModel implements Serializable {

    static final IndexMap<Character> CASE_SENS_SYMBOLS;
    static final Set<Character> CASE_SENS_CHARS;
    static final IndexMap<Character> CASE_INSENS_SYMBOLS;
    static final Set<Character> CASE_INSENS_CHARS;

    static {
        Set<Character> symbols = "abcdefghijklmnopqrstuvwxyz.-ABCDEFGHIJKLMNOPQRSTUVWXYZ0?^$".chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
        CASE_SENS_SYMBOLS = new HashIndexMap<>(symbols);
        CASE_SENS_CHARS = "abcdefghijklmnopqrstuvwxyz.-ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());

        Set<Character> caseInsensSymbols = "abcdefghijklmnopqrstuvwxyz.-0?^$".chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
        CASE_INSENS_SYMBOLS = new HashIndexMap<>(caseInsensSymbols);
        CASE_INSENS_CHARS = "abcdefghijklmnopqrstuvwxyz.-".chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
    }

    // Log probabilities that certain character trigrams are an abbreviation or a longform
    private final double[][][] abbrevProbs;

    private final double[][][] longformProbs;

    private final boolean caseSensitive;

    private final Set<String> longformsLower;

    private final transient IndexMap<Character> symbols;

    private final transient Set<Character> chars;

    private OrthographicAcronymModel(double[][][] abbrevProbs, double[][][] longformProbs, boolean caseSensitive, Set<String> longformsLower) {
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
    boolean seemsLikeAbbreviation(Token token) {

        String wordRaw = token.text();
        String wordLower = wordRaw.toLowerCase();

        // Check to see if it's a long form first
        // This is case-insensitive to curb overzealous tagging of abbreviations
        // Also check the normal form, if it exists, as affixed forms may not appear in the list of long forms
        if (longformsLower != null && (longformsLower.contains(wordLower))) {
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
    private boolean seemsLikeAbbrevByTrigram(String form) {
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
    private double getWordLikelihood(String word, double[][][] probs) {
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
        } else if (!chars.contains(c)) {
            c = '?';
        }
        return c;
    }

    /**
     * Loads the orthographic model.
     *
     * @since 1.5.0
     */
    @Singleton
    static class Loader extends DataLoader<OrthographicAcronymModel> {
        private final Path orthographicModel;

        private IndexMap<Character> symbols;

        @Inject
        Loader(@Setting("acronym.orthographicModel.path") Path orthographicModel) {
            this.orthographicModel = orthographicModel;
        }

        @Override
        protected OrthographicAcronymModel loadModel() throws BiomedicusException {
            Yaml yaml = new Yaml();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> serObj = (Map<String, Object>) yaml.load(Files.newBufferedReader(orthographicModel));
                boolean caseSensitive = (Boolean) serObj.get("caseSensitive");
                symbols = caseSensitive ? CASE_SENS_SYMBOLS : CASE_INSENS_SYMBOLS;
                @SuppressWarnings("unchecked")
                Map<String, Double> abbrevProbsMap = (Map<String, Double>) serObj.get("abbrevProbs");
                double[][][] abbrevProbs = expandProbs(abbrevProbsMap);
                @SuppressWarnings("unchecked")
                Map<String, Double> longformProbsMap = (Map<String, Double>) serObj.get("longformProbs");
                double[][][] longformProbs = expandProbs(longformProbsMap);
                Set<String> longformsLower = new HashSet<>();
                @SuppressWarnings("unchecked")
                List<String> longformsLowerList = (List<String>) serObj.get("longformsLower");
                longformsLower.addAll(longformsLowerList);
                return new OrthographicAcronymModel(abbrevProbs, longformProbs, caseSensitive, longformsLower);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }

        private double[][][] expandProbs(Map<String, Double> collapsedProbs) {
            double[][][] probs = new double[symbols.size()][symbols.size()][symbols.size()];
            for (Map.Entry<String, Double> entry : collapsedProbs.entrySet()) {
                String key = entry.getKey();
                probs[symbols.indexOf(key.charAt(0))][symbols.indexOf(key.charAt(1))][symbols.indexOf(key.charAt(2))] = entry.getValue();
            }
            return probs;
        }
    }
}
