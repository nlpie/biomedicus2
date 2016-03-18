package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.common.collect.MetricTree;
import edu.umn.biomedicus.common.collect.StandardEditDistance;
import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.grams.Trigram;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Singleton
public class SpellingModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private static class SuggestionProbability {
        private final String suggestion;
        private final double probability;

        public SuggestionProbability(String suggestion, double probability) {
            this.suggestion = suggestion;
            this.probability = probability;
        }
    }

    private final MetricTree<String> termsTree;

    private final Map<Trigram<String>, Double> trigramProbabilities;

    private final Map<Bigram<String>, Double> bigramBackOffs;

    private final Map<String, Double> unigramProbabilities;

    private final int maxEditDistance;

    @Inject
    SpellingModel(BiomedicusConfiguration biomedicusConfiguration, Vocabulary vocabulary) throws IOException {
        TermIndex wordIndex = vocabulary.wordIndex();
        LOGGER.info("Building BK tree for spelling model using {} words.", wordIndex.size());
        MetricTree.Builder<String> builder = MetricTree.builder();
        builder.withMetric(StandardEditDistance.levenstein());
        wordIndex.stream().map(wordIndex::getString).forEach(builder::add);
        termsTree = builder.build();

        Path arpaPath = biomedicusConfiguration.resolveDataFile("spelling.arpa.path");
        LOGGER.info("Loading Spelling n-grams from ARPA file: {}", arpaPath);

        Pattern unigramPattern = Pattern.compile("(\\-?[0-9]\\.[0-9]{4}) ([\\p{IsAlphabetic}]+)\\t(\\-?[0-9]\\.[0-9]{4})");
        Pattern bigramPattern = Pattern.compile("(\\-?[0-9]\\.[0-9]{4}) ([\\p{IsAlphabetic}]+) ([\\p{IsAlphabetic}]+) (\\-?[0-9]\\.[0-9]{4})");
        Pattern trigramPattern = Pattern.compile("(\\-?[0-9]\\.[0-9]{4}) ([\\p{IsAlphabetic}]+) ([\\p{IsAlphabetic}]+) ([\\p{IsAlphabetic}]+)");

        unigramProbabilities = new HashMap<>();
        bigramBackOffs = new HashMap<>();
        trigramProbabilities = new HashMap<>();

        Files.lines(arpaPath).forEach(s -> {
            Matcher unigramMatcher = unigramPattern.matcher(s);
            if (unigramMatcher.find()) {
                double probability = Double.parseDouble(unigramMatcher.group(1));
                String unigram = unigramMatcher.group(2).toLowerCase();
                Double existing = unigramProbabilities.get(unigram);
                if (existing != null) {
                    unigramProbabilities.put(unigram, probability * existing);
                } else {
                    unigramProbabilities.put(unigram, probability);
                }
                return;
            }

            Matcher bigramMatcher = bigramPattern.matcher(s);
            if (bigramMatcher.find()) {
                String first = bigramMatcher.group(2).toLowerCase();
                String second = bigramMatcher.group(3).toLowerCase();
                double backOff = Double.parseDouble(bigramMatcher.group(4));

                Ngram<String> bigram = Ngram.create(first, second);
                Double existing = bigramBackOffs.get(bigram);
                if (existing != null) {
                    bigramBackOffs.put(bigram, backOff * existing);
                } else {
                    bigramBackOffs.put(bigram, backOff);
                }
                return;
            }

            Matcher trigramMatcher = trigramPattern.matcher(s);
            if (trigramMatcher.find()) {
                double probability = Double.parseDouble(trigramMatcher.group(1));
                String first = trigramMatcher.group(2);
                String second = trigramMatcher.group(3);
                String third = trigramMatcher.group(4);

                Ngram<String> trigram = Ngram.create(first, second, third);
                Double existing = bigramBackOffs.get(trigram);
                if (existing != null) {
                    trigramProbabilities.put(trigram, probability * existing);
                } else {
                    trigramProbabilities.put(trigram, probability);
                }

                trigramProbabilities.put(trigram, probability);
            }
        });

        maxEditDistance = biomedicusConfiguration.getSettings().getAsInt("spelling.maxEditDistance");
    }

    /**
     * Suggests a correction.
     *
     * @param misspelledWord lowercase of the misspelled word.
     * @param context two immediate previous lowercase words.
     * @return a correction for the misspelled word.
     */
    @Nullable
    public String suggestCorrection(String misspelledWord, Bigram<String> context) {
        Optional<SuggestionProbability> optimalSuggestion = termsTree.search(misspelledWord, maxEditDistance)
                .map(suggestion -> {
                    Trigram<String> trigram = Ngram.create(context.getFirst(), context.getSecond(), misspelledWord);
                    Double trigramProbability = trigramProbabilities.get(trigram);
                    if (trigramProbability != null) {
                        return new SuggestionProbability(suggestion, trigramProbability);
                    }

                    Double unigramProbability = unigramProbabilities.get(misspelledWord);
                    if (unigramProbability == null) {
                        return new SuggestionProbability(suggestion, Double.NEGATIVE_INFINITY);
                    }

                    Bigram<String> bigram = trigram.head();
                    Double bigramBackOff = bigramBackOffs.get(bigram);
                    if (bigramBackOff != null) {
                        double probability = unigramProbability * bigramBackOff;
                        return new SuggestionProbability(suggestion, probability);
                    } else {
                        return new SuggestionProbability(suggestion, unigramProbability);
                    }
                })
                .filter(suggestionProbability -> suggestionProbability.probability != Double.NEGATIVE_INFINITY)
                .sorted()
                .findFirst();

        return optimalSuggestion != null ? optimalSuggestion.get().suggestion: null;
    }
}
