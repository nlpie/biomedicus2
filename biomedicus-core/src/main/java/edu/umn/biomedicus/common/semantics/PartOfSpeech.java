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
}
