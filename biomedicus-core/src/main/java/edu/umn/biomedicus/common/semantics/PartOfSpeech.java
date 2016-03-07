/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.semantics;

import java.util.*;

/**
 * A part of speech in the Penn Treebank P.O.S. tags format. The full list of tags is described here:
 * <a href="http://repository.upenn.edu/cgi/viewcontent.cgi?article=1603&context=cis_reports">
 * Santorini, B. 1990. Part-of-speech tagging guidelines for the Penn Treebank Project.
 * Technical report MS-CIS-90-47, Department of Computer and Information Science, University of Pennsylvania.</a>
 * <p/>
 * <p>We use the tag as the enum constant for the most case, except in cases where the enum constant can not be
 * represented in Java</p>
 * <p/>
 * <p>A notable derivation from Penn Treebank is the Before Beginning of Sentence/BBS tag, Beginning of Sentence/BOS tag
 * and the End of Sentence/EOS tag, which are abstract tags (i.e. should never be used to tag a token) that are used in
 * some statistical models like the TnT POS-Tagger that are run after sentence splitting and can be improved by the
 * inclusion of sentence boundary information.</p>
 */
public enum PartOfSpeech {
    /**
     * Coordinating conjunction
     */
    CC("CC"),

    /**
     * Cardinal number
     */
    CD("CD"),

    /**
     * Determiner
     */
    DT("DT"),

    /**
     * Existential <i>there</i>
     */
    EX("EX"),

    /**
     * Foreign word
     */
    FW("FW"),

    /**
     * Preposition or coordinating conjunction
     */
    IN("IN"),

    /**
     * Adjective
     */
    JJ("JJ"),

    /**
     * Adjective, comparative
     */
    JJR("JJR"),

    /**
     * Adjective, superlative
     */
    JJS("JJS"),

    /**
     * List item marker
     */
    LS("LS"),

    /**
     * Modal
     */
    MD("MD"),

    /**
     * Noun, singular or mass
     */
    NN("NN"),

    /**
     * Noun, plural
     */
    NNS("NNS"),

    /**
     * Proper noun, singular
     */
    NNP("NNP"),

    /**
     * Proper noun, plural
     */
    NNPS("NNPS"),

    /**
     * Predeterminer
     */
    PDT("PDT"),

    /**
     * Possessive ending
     */
    POS("POS"),

    /**
     * Personal pronoun
     */
    PRP("PRP"),

    /**
     * Possessive pronoun
     */
    PRP$("PRP$"),

    /**
     * Adverb
     */
    RB("RB"),

    /**
     * Adverb, comparative
     */
    RBR("RBR"),

    /**
     * Adverb, superlative
     */
    RBS("RBS"),

    /**
     * Particle
     */
    RP("RP"),

    /**
     * Symbol
     */
    SYM("SYM"),

    /**
     * <i>to</i>
     */
    TO("TO"),

    /**
     * Interjection
     */
    UH("UH"),

    /**
     * Verb, base form
     */
    VB("VB"),

    /**
     * Verb, past tense
     */
    VBD("VBD"),

    /**
     * Verb, gerund or present participle
     */
    VBG("VBG"),

    /**
     * Verb, past participle
     */
    VBN("VBN"),

    /**
     * Verb, non-3rd person singular present
     */
    VBP("VBP"),

    /**
     * Verb, 3rd person singular present
     */
    VBZ("VBZ"),

    /**
     * Wh-determiner
     */
    WDT("WDT"),

    /**
     * Wh-pronoun
     */
    WP("WP"),

    /**
     * Possessive wh-pronoun
     */
    WP$("WP$"),

    /**
     * Wh-adverb
     */
    WRB("WRB"),

    /**
     * Punctuation, sentence closer
     */
    SENTENCE_CLOSER_PUNCTUATION("."),

    /**
     * Punctuation, comma
     */
    COMMA_PUNCTUATION(","),

    /**
     * Punctuation, colon
     */
    COLON_PUNCTUATION(":"),

    /**
     * Punctuation, left parenthesis
     */
    LEFT_PAREN("("),

    /**
     * Punctuation, right parenthesis
     */
    RIGHT_PAREN(")"),

    /**
     * Punctuation, right quotation marks
     */
    OPENING_QUOTATION("``"),

    /**
     * Punctuation, right quotation marks
     */
    CLOSING_QUOTATION("''"),

    /**
     * Punctuation opening single quote.
     */
    OPENING_SINGLE_QUOTE("`"),

    /**
     * Punctuation, closing single quote.
     */
    CLOSING_SINGLE_QUOTE("'"),

    /**
     * Punctuation, straight double quote
     */
    STRAIGHT_DOUBLE_QUOTE("\""),

    /**
     * Pound sign #
     */
    POUND_SIGN("#"),

    /**
     * Dollar sign $
     */
    DOLLAR_SIGN("$"),

    /**
     * Hyphen
     */
    HYPH("HYPH"),

    /**
     * Affix
     */
    AFX("AFX"),

    /**
     * Goes-with
     */
    GW("GW"),

    /**
     * Missing, Unknown, De-identified, or untaggable
     */
    XX("XX"),

    /**
     * Before Beginning of Sentence, abstract tag used for before the beginning of a sentence.
     */
    BBS("BBS"),

    /**
     * Beginning of Sentence, abstract tag used for the beginning of a sentence.
     */
    BOS("BOS"),

    /**
     * End of Sentence, abstract tag used for the end of a sentence.
     */
    EOS("EOS");

    private final String pos;

    /**
     * Default constructor. Initialized with the string representation of the part of speech tag.
     *
     * @param pos string value for the part of speech tag
     */
    PartOfSpeech(String pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return pos;
    }

    /**
     * A map from the Penn treebank tag to the Biomedicus PartOfSpeech enumerated object value
     */
    public static final Map<String, PartOfSpeech> MAP = buildMap();

    public static final Map<String, PartOfSpeech> FALLBACK_MAP = buildFallbackMap();

    private static Map<String, PartOfSpeech> buildFallbackMap() {

        Map<String, PartOfSpeech> builder = new HashMap<>();
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            builder.put(partOfSpeech.pos, partOfSpeech);
        }
        builder.put("N", NN);
        builder.put("NP", NNP);
        builder.put("NPS", NNPS);
        builder.put("PP", PRP);
        builder.put("PP$", PRP$);
        builder.put("-", HYPH);
        builder.put("--", COLON_PUNCTUATION);
        builder.put(null, XX);
        builder.put("null", XX);
        builder.put("", XX);
        builder.put("X", XX);
        builder.put("“", OPENING_QUOTATION);
        builder.put("”", CLOSING_QUOTATION);
        builder.put("‘", OPENING_SINGLE_QUOTE);
        builder.put("’", CLOSING_SINGLE_QUOTE);
        builder.put("″", STRAIGHT_DOUBLE_QUOTE);

        return Collections.unmodifiableMap(builder);
    }

    private static Map<String, PartOfSpeech> buildMap() {
        Map<String, PartOfSpeech> builder = new HashMap<>();
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            builder.put(partOfSpeech.pos, partOfSpeech);
        }

        return Collections.unmodifiableMap(builder);
    }

    /**
     * Verb-like parts of speech
     */
    public static final Set<PartOfSpeech> VERB_CLASS;

    static {
        EnumSet<PartOfSpeech> verbs = EnumSet.of(
                VB,
                VBD,
                VBG,
                VBN,
                VBP,
                VBZ
        );
        VERB_CLASS = Collections.unmodifiableSet(verbs);
    }

    /**
     * Parts of speech that represent punctuation in text
     */
    public static final Set<PartOfSpeech> PUNCTUATION_CLASS;

    static {
        EnumSet<PartOfSpeech> punc = EnumSet.of(
                SENTENCE_CLOSER_PUNCTUATION,
                COMMA_PUNCTUATION,
                COLON_PUNCTUATION,
                LEFT_PAREN,
                RIGHT_PAREN
        );
        PUNCTUATION_CLASS = Collections.unmodifiableSet(punc);
    }

    /**
     * Parts of speech that are part of the open class, i.e. most commonly accept the addition of new words to a
     * language.
     */
    public static final Set<PartOfSpeech> OPEN_CLASS;

    static {
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
        OPEN_CLASS = Collections.unmodifiableSet(open);
    }

    /**
     * Parts of speech that are real, i.e. correctly
     */
    public static final Set<PartOfSpeech> REAL_TAGS;

    static {
        EnumSet<PartOfSpeech> reals = EnumSet.allOf(PartOfSpeech.class);
        reals.remove(XX);
        reals.remove(BBS);
        reals.remove(BOS);
        reals.remove(EOS);
        REAL_TAGS = Collections.unmodifiableSet(reals);
    }
}
