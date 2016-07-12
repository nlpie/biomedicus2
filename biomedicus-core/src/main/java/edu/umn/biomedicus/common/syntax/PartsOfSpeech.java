/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.syntax;

import java.util.*;

import static edu.umn.biomedicus.common.syntax.PartOfSpeech.*;
import static edu.umn.biomedicus.common.syntax.UniversalPartOfSpeech.*;

/**
 * A utility class for categorizing and working with parts of speech.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class PartsOfSpeech {

    private static final Map<String, PartOfSpeech> MAP = buildMap();

    private static Map<String, PartOfSpeech> buildMap() {
        Map<String, PartOfSpeech> builder = new HashMap<>();
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            builder.put(partOfSpeech.toString(), partOfSpeech);
        }

        return Collections.unmodifiableMap(builder);
    }

    private static final Map<String, PartOfSpeech> FALLBACK_MAP = buildFallbackMap();

    private static Map<String, PartOfSpeech> buildFallbackMap() {
        Map<String, PartOfSpeech> builder = new HashMap<>();
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            builder.put(partOfSpeech.toString(), partOfSpeech);
        }
        builder.put("N", NN);
        builder.put("NP", PartOfSpeech.NNP);
        builder.put("NPS", PartOfSpeech.NNPS);
        builder.put("PP", PartOfSpeech.PRP);
        builder.put("PP$", PartOfSpeech.PRP$);
        builder.put("-", PartOfSpeech.HYPH);
        builder.put("--", PartOfSpeech.COLON_PUNCTUATION);
        builder.put(null, PartOfSpeech.XX);
        builder.put("null", PartOfSpeech.XX);
        builder.put("", PartOfSpeech.XX);
        builder.put("X", PartOfSpeech.XX);
        builder.put("“", PartOfSpeech.OPENING_QUOTATION);
        builder.put("”", PartOfSpeech.CLOSING_QUOTATION);
        builder.put("‘", PartOfSpeech.OPENING_SINGLE_QUOTE);
        builder.put("’", PartOfSpeech.CLOSING_SINGLE_QUOTE);
        builder.put("″", PartOfSpeech.STRAIGHT_DOUBLE_QUOTE);

        return Collections.unmodifiableMap(builder);
    }

    private static final Set<PartOfSpeech> PUNCTUATION_CLASS = buildPunctuationClass();

    private static Set<PartOfSpeech> buildPunctuationClass() {
        EnumSet<PartOfSpeech> punc = EnumSet.of(
                SENTENCE_CLOSER_PUNCTUATION,
                COMMA_PUNCTUATION,
                COLON_PUNCTUATION,
                LEFT_PAREN,
                RIGHT_PAREN,
                OPENING_QUOTATION,
                CLOSING_QUOTATION,
                OPENING_SINGLE_QUOTE,
                CLOSING_SINGLE_QUOTE,
                STRAIGHT_DOUBLE_QUOTE,
                HYPH,
                POUND_SIGN,
                DOLLAR_SIGN
        );
        return Collections.unmodifiableSet(punc);
    }

    private static final Set<PartOfSpeech> OPEN_CLASS = buildOpenClass();

    private static Set<PartOfSpeech> buildOpenClass() {
        EnumSet<PartOfSpeech> open = EnumSet.of(
                JJ,
                JJR,
                JJS,
                NN,
                NNP,
                NNPS,
                RB,
                RBR,
                RBS,
                VB,
                VBD,
                VBG,
                VBN,
                VBP,
                VBZ
        );
        return Collections.unmodifiableSet(open);
    }

    private static final Set<PartOfSpeech> REAL_TAGS = buildRealTags();

    private static Set<PartOfSpeech> buildRealTags() {
        EnumSet<PartOfSpeech> reals = EnumSet.allOf(PartOfSpeech.class);
        reals.remove(XX);
        reals.remove(BBS);
        reals.remove(BOS);
        reals.remove(EOS);
        return Collections.unmodifiableSet(reals);
    }

    private static final Set<UniversalPartOfSpeech> UNIVERSAL_OPEN_CLASS = buildUniversalOpenClass();

    private static Set<UniversalPartOfSpeech> buildUniversalOpenClass() {
        EnumSet<UniversalPartOfSpeech> openClass = EnumSet.of(
                ADJ,
                ADV,
                INTJ,
                NOUN,
                PROPN,
                VERB
        );
        return Collections.unmodifiableSet(openClass);
    }

    private static final Set<UniversalPartOfSpeech> UNIVERSAL_CLOSED_CLASS = buildUniversalClosedClass();

    private static Set<UniversalPartOfSpeech> buildUniversalClosedClass() {
        EnumSet<UniversalPartOfSpeech> closedClass = EnumSet.of(
                ADP,
                AUX,
                CONJ,
                DET,
                NUM,
                PART,
                PRON,
                SCONJ
        );
        return Collections.unmodifiableSet(closedClass);
    }

    private PartsOfSpeech() {
        throw new UnsupportedOperationException("Instantiation of utility class.");
    }


    /**
     * Returns the part of speech for the given penn tag.
     *
     * @param tag penn tag
     * @return the part of speech for the tag.
     */
    public static PartOfSpeech forTag(String tag) {
        if (!MAP.containsKey(tag)) {
            throw new IllegalArgumentException("Part of speech not found: " + tag);
        }
        return MAP.get(tag);
    }

    public static Optional<PartOfSpeech> forTagWithFallback(String tag) {
        PartOfSpeech partOfSpeech = MAP.get(tag);
        if (partOfSpeech != null) return Optional.of(partOfSpeech);
        return Optional.ofNullable(FALLBACK_MAP.get(tag));
    }

    /**
     * Returns the penn tag for the given part of speech.
     *
     * @param partOfSpeech the part of speech
     * @return the penn tag
     */
    public static String tagForPartOfSpeech(PartOfSpeech partOfSpeech) {
        return partOfSpeech.toString();
    }

    public static Set<UniversalPartOfSpeech> getUniversalOpenClass() {
        return UNIVERSAL_OPEN_CLASS;
    }

    public static Set<UniversalPartOfSpeech> getUniversalClosedClass() {
        return UNIVERSAL_CLOSED_CLASS;
    }

    public static Set<PartOfSpeech> getOpenClass() {
        return OPEN_CLASS;
    }

    public static Set<PartOfSpeech> getRealTags() {
        return REAL_TAGS;
    }

    public static Set<PartOfSpeech> getPunctuationClass() {
        return PUNCTUATION_CLASS;
    }

    public static UniversalPartOfSpeech universalForTag(String tag) {
        return UniversalPartOfSpeech.valueOf(tag);
    }
}
