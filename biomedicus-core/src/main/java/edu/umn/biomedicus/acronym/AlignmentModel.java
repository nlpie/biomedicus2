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
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Will attempt to determine a long form candidate for an unknown abbreviation by a sequence matching algorithm
 * Uses a modified Needleman-Wunsch algorithm, with parameters chosen with abbreviations/acronyms in mind
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@ProvidedBy(AlignmentModel.Loader.class)
class AlignmentModel implements Serializable {

    public static void main(String[] args) throws IOException {
        Path longformsPath = Paths.get(args[0]);
        Path outPath = Paths.get(args[1]);
        boolean caseSensitive = args.length > 2;
        create(longformsPath, caseSensitive).serialize(outPath);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentModel.class);

    private List<String> longforms;
    private boolean caseSensitive;
    private transient Map<String, Double> cachedScores = new HashMap<>();

    public List<String> getLongforms() {
        return longforms;
    }

    public void setLongforms(List<String> longforms) {
        this.longforms = longforms;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    private AlignmentModel(List<String> longforms, boolean caseSensitive) {
        this.longforms = longforms;
        this.caseSensitive = caseSensitive;
    }

    public void serialize(Path outPath) throws IOException {
        Yaml yaml = YamlSerialization.createYaml();
        FileWriter writer = new FileWriter(outPath.toFile());
        yaml.dump(this, writer);
        writer.close();
    }

    public static AlignmentModel create(Path longformsPath, boolean caseSensitive) throws IOException {
        Set<String> longforms = Files.lines(longformsPath).collect(Collectors.toSet());
        return new AlignmentModel(new ArrayList<>(longforms), caseSensitive);
    }

    public static AlignmentModel create(Path longformsPath) throws IOException {
        return AlignmentModel.create(longformsPath, false);
    }

    /**
     * Calculate the score of an optimal alignment using the Needleman-Wunsch algorithm
     *
     * @param abbr     the abbreviation to align
     * @param longform the longform to align
     * @return the score of the match
     */
    private double align(String abbr, String longform) {
        if (!caseSensitive) {
            abbr = abbr.toLowerCase();
            longform = longform.toLowerCase();
        }

        // Scores and penalties:
        // For matching a character between abbreviation and longform
        double match = 1;
        // For matching after a space or hyphen in the longform
        double wordInitialMatch = 3;
        // For matching at the beginning of both strings (highly preferred)
        double initialMatch = 5;
        // For deleting a character from the abbreviation (abbrevs will rarely have additional characters)
        double delAbbr = -3;
        // For deleting a character from the long form (happens all the time)
        double delLong = 0;
        // For deleting a word-initial character from a long form (happens less often)
        double delLongWordInitial = -2;

        List<Character> breakingChars = new ArrayList<>();
        Collections.addAll(breakingChars, ' ', '-', '/');

        int m = abbr.length();
        int n = longform.length();
        double[][] matrix = new double[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {

            for (int j = 0; j <= n; j++) {

                double max = -Double.MAX_VALUE;
                if (i == 0 && j == 0)
                    max = 0;

                if (i > 0 && j > 0) {
                    // Don't even consider mismatches (departure from standard Needleman-Wunsch)
                    if (abbr.charAt(i - 1) == longform.charAt(j - 1)) {
                        double diagScore = matrix[i - 1][j - 1];
                        if (j == 1 && i == 1) {
                            diagScore += initialMatch;
                        } else if (j < 2 || breakingChars.contains(longform.charAt(j - 2))) {
                            diagScore += wordInitialMatch;
                        } else {
                            diagScore += match;
                        }
                        if (diagScore > max)
                            max = diagScore;
                    }
                }
                if (i > 0) {
                    double downScore = matrix[i - 1][j] + delAbbr;
                    if (downScore > max)
                        max = downScore;
                }
                if (j > 0) {
                    double rightScore = matrix[i][j - 1];
                    if (j < 2 || breakingChars.contains(longform.charAt(j - 2))) {
                        rightScore += delLongWordInitial;
                    } else {
                        rightScore += delLong;
                    }
                    if (rightScore > max)
                        max = rightScore;
                }
                matrix[i][j] = max;
            }
        }

        return matrix[m][n];
    }

    /**
     * Will return the highest-scoring longform by alignment
     * If there are ties, only the last one will be returned
     *
     * @param abbrev the abbreviation to expand
     * @return the last longform with the highest score
     */
    String findBestLongform(String abbrev) {
        String best = abbrev;
        double maxScore = -Double.MAX_VALUE;
        for (String longform : longforms) {
            double thisScore = align(abbrev, longform);
            if (thisScore > maxScore) {
                maxScore = thisScore;
                best = longform;
            }
        }
        return best;
    }

    /**
     * Will return the highest-scoring longforms by alignment
     * All tied candidates will be present in the List
     *
     * @param abbrev the abbreviation to expand
     * @return all longforms with the highest score
     */
    List<String> findBestLongforms(String abbrev) {
        List<String> best = new ArrayList<>();
        double maxScore = -Double.MAX_VALUE;
        for (String longform : longforms) {
            double thisScore = align(abbrev, longform);
            if (thisScore > maxScore) {
                maxScore = thisScore;
                best = new ArrayList<>();
            }
            if (thisScore == maxScore)
                best.add(longform);
        }
        return best;
    }

    /**
     * Get longforms ranked by their score
     *
     * @param abbrev the abbreviation to expand
     * @return a TreeMap of longforms to their scores, ordered by score
     */
    Map<String, Double> rankLongforms(String abbrev) {
        HashMap<String, Double> longformScores = new LinkedHashMap<>();
        for (String longform : longforms) {
            longformScores.put(longform, align(abbrev, longform));
        }
        TreeMap<String, Double> sortedScores = new TreeMap<>((o1, o2) -> {
            int comparison = longformScores.get(o2).compareTo(longformScores.get(o1));
            if (comparison == 0)
                comparison = o1.compareTo(o2);
            return comparison;
        });
        sortedScores.putAll(longformScores);

        return sortedScores;
    }

    /**
     *
     */
    @Singleton
    static class Loader extends DataLoader<AlignmentModel> {

        private final Path modelPath;

        @Inject
        public Loader(@Setting("acronym.alignmentModel.path") Path modelPath) {
            this.modelPath = modelPath;
        }

        @Override
        protected AlignmentModel loadModel() throws BiomedicusException {
            LOGGER.info("Loading acronym alignment model: {}", modelPath);

            Yaml yaml = YamlSerialization.createYaml();

            try {
                return (AlignmentModel) yaml.load(Files.newBufferedReader(modelPath));
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
