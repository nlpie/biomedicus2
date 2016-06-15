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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.tuples.WordPos;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Responsible for loading a SPECIALIST LRAGR file and creating a normalizer from it.
 *
 * @author Ben Knoll
 * @author Serguei Pakhomov
 */
public class NormalizerModelBuilder {

    private static final int IGNORE_WHEN_LONGER = 100;

    private static final Map<LragrPos, PartOfSpeech> LRAGR_TO_PENN;

    static {
        Map<LragrPos, PartOfSpeech> builder = new HashMap<>();
        builder.put(new LragrPos("noun", "uncount(thr_plur)"), PartOfSpeech.NNS);
        builder.put(new LragrPos("noun", "count(thr_plur)"), PartOfSpeech.NNS);
        builder.put(new LragrPos("noun", "uncount(thr_sing)"), PartOfSpeech.NN);
        builder.put(new LragrPos("noun", "count(thr_sing)"), PartOfSpeech.NN);
        builder.put(new LragrPos("verb", "infinitive"), PartOfSpeech.VB);
        builder.put(new LragrPos("verb", "pres(thr_sing)"), PartOfSpeech.VBZ);
        builder.put(new LragrPos("verb", "past"), PartOfSpeech.VBD);
        builder.put(new LragrPos("verb", "past_part"), PartOfSpeech.VBN);
        builder.put(new LragrPos("verb", "pres_part"), PartOfSpeech.VBG);
        builder.put(new LragrPos("adj", "comparative"), PartOfSpeech.JJR);
        builder.put(new LragrPos("adj", "superlative"), PartOfSpeech.JJS);
        builder.put(new LragrPos("adj", "positive"), PartOfSpeech.JJ);
        builder.put(new LragrPos("adv", "comparative"), PartOfSpeech.RBR);
        builder.put(new LragrPos("adv", "superlative"), PartOfSpeech.RBS);
        builder.put(new LragrPos("adv", "positive"), PartOfSpeech.RB);
        LRAGR_TO_PENN = Collections.unmodifiableMap(builder);
    }

    private static final Map<LragrPos, PartOfSpeech> LRAGR_TO_PENN_FALLBACK;

    static {
        Map<LragrPos, PartOfSpeech> builder = new HashMap<>();
        builder.put(new LragrPos("noun", "uncount(thr_plur)"), PartOfSpeech.NN);
        builder.put(new LragrPos("noun", "count(thr_plur)"), PartOfSpeech.NN);
        builder.put(new LragrPos("noun", "uncount(thr_sing)"), PartOfSpeech.NNS);
        builder.put(new LragrPos("noun", "count(thr_sing)"), PartOfSpeech.NNS);
        LRAGR_TO_PENN_FALLBACK = Collections.unmodifiableMap(builder);
    }

    /**
     * Index of the inflectional variant (Term to lookup) in the LRAGR table.
     */
    public static final int LRAGR_INFLECTIONAL_VARIANT = 1;

    /**
     * Index of the syntactic category (part of speech) in the LRAGR table.
     */
    public static final int LRAGR_SYNTACTIC_CATEGORY = 2;

    /**
     * Index of the agreement inflection code in the LRAGR table.
     */
    public static final int LRAGR_AGREEMENT_INFLECTION_CODE = 3;

    /**
     * Index of the base for in the LRAGR table.
     */
    public static final int LRAGR_BASE_FORM = 4;

    private final Path lragrFile;

    private final Path lexiconFile;

    private final Path fallbackLexiconFile;

    public NormalizerModelBuilder(Path lragrFile, Path lexiconFile, Path fallbackLexiconFile) {
        this.lragrFile = lragrFile;
        this.lexiconFile = lexiconFile;
        this.fallbackLexiconFile = fallbackLexiconFile;
    }

    public void process() throws IOException {
        Map<WordPos, String> lexiconBuilder = new HashMap<>();
        Map<WordPos, String> fallbackLexiconBuilder = new HashMap<>();
        Pattern exclusionPattern = Pattern.compile(".*[\\|\\$#,@;:<>\\?\\[\\]\\{\\}\\d\\.].*");

        Set<String> visited = new HashSet<>();
        Files.lines(lragrFile)
                .map(line -> line.split("\\|"))
                .forEach(lragrArray -> {
                    String inflectionalVariant = lragrArray[LRAGR_INFLECTIONAL_VARIANT].trim().toLowerCase();

                    Matcher exclusionMatcher = exclusionPattern.matcher(inflectionalVariant);
                    if (visited.contains(inflectionalVariant) || exclusionMatcher.matches() || inflectionalVariant.length() > IGNORE_WHEN_LONGER) {
                        return;
                    }
                    visited.add(inflectionalVariant);

                    String syntacticCategory = lragrArray[LRAGR_SYNTACTIC_CATEGORY].trim();
                    String agreementInflectionCode = lragrArray[LRAGR_AGREEMENT_INFLECTION_CODE].trim();
                    String baseForm = lragrArray[LRAGR_BASE_FORM].trim();

                    LragrPos lragrPos = new LragrPos(syntacticCategory, agreementInflectionCode);

                    if (!inflectionalVariant.endsWith(baseForm)) {
                        PartOfSpeech pennPos = LRAGR_TO_PENN.get(lragrPos);
                        if (pennPos != null) {
                            lexiconBuilder.put(new WordPos(inflectionalVariant, pennPos), baseForm);
                        }

                        PartOfSpeech fallbackPos = LRAGR_TO_PENN_FALLBACK.get(lragrPos);
                        if (fallbackPos != null) {
                            fallbackLexiconBuilder.put(new WordPos(inflectionalVariant, fallbackPos), baseForm);
                        }
                    }
                });

        Files.createDirectories(lexiconFile.getParent());
        Files.createDirectories(fallbackLexiconFile.getParent());

        Yaml yaml = new Yaml();
        try (BufferedWriter output = Files.newBufferedWriter(lexiconFile, CREATE, TRUNCATE_EXISTING)) {
            yaml.dump(lexiconBuilder, output);
        }
        try (BufferedWriter output = Files.newBufferedWriter(fallbackLexiconFile, CREATE, TRUNCATE_EXISTING)) {
            yaml.dump(fallbackLexiconBuilder, output);
        }
    }

    public static void main(String[] args) {
        Path lragrFile = Paths.get(args[0]);
        Path lexiconFile = Paths.get(args[1]);
        Path fallbackLexiconFile = Paths.get(args[2]);

        NormalizerModelBuilder builder = new NormalizerModelBuilder(lragrFile, lexiconFile, fallbackLexiconFile);

        try {
            builder.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LragrPos implements Comparable<LragrPos> {
        private final String syntacticCategory;
        private final String agreementInflectionCode;

        public LragrPos(String syntacticCategory, String agreementInflectionCode) {
            this.syntacticCategory = Objects.requireNonNull(syntacticCategory);
            this.agreementInflectionCode = Objects.requireNonNull(agreementInflectionCode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LragrPos lragrPos = (LragrPos) o;

            if (!agreementInflectionCode.equals(lragrPos.agreementInflectionCode)) return false;
            return syntacticCategory.equals(lragrPos.syntacticCategory);

        }

        @Override
        public int hashCode() {
            int result = syntacticCategory.hashCode();
            result = 31 * result + agreementInflectionCode.hashCode();
            return result;
        }

        @Override
        public int compareTo(LragrPos o) {
            int result = this.syntacticCategory.compareTo(o.syntacticCategory);
            if (result == 0) result = this.agreementInflectionCode.compareTo(o.agreementInflectionCode);
            return result;
        }
    }
}
