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

package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.text.Term;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.model.tokensets.OrderedTokenSet;
import edu.umn.biomedicus.model.tokensets.SentenceTextOrderedTokenSet;
import edu.umn.biomedicus.model.tokensets.TextOrderedTokenSet;
import edu.umn.biomedicus.application.DocumentProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.umn.biomedicus.model.semantics.PartOfSpeech.*;

/**
 * Uses a {@link edu.umn.biomedicus.concepts.ConceptModel} to recognize concepts in text. First, it will
 * try to find direct matches against all in-order sublists of tokens in a sentence. Then it will perform syntactic
 * permutations on any prepositional phrases in those sublists.
 *
 * @since 1.0.0
 * @author Ben Knoll
 * @author Serguei Pakhomov
 */
@DocumentScoped
class DictionaryConceptRecognizer implements DocumentProcessor {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Trivial parts of speech.
     */
    private static final Set<PartOfSpeech> TRIVIAL_POS;

    /**
     * Initialization for trivial parts of speech.
     */
    static {
        Set<PartOfSpeech> builder = new HashSet<>();
        Collections.addAll(builder,
                DT,
                CD,
                WDT,
                TO,
                CC,
                PRP,
                PRP$,
                MD,
                EX,
                IN);
        TRIVIAL_POS = Collections.unmodifiableSet(builder);
    }

    /**
     * Auxiliary verbs.
     */
    private static final Set<String> AUXILIARY_VERBS;

    /**
     * Initialization for auxiliary verbs.
     */
    static {
        Set<String> builder = new HashSet<>();
        Collections.addAll(builder,
                "is",
                "was",
                "were",
                "am",
                "are",
                "will",
                "was",
                "be",
                "been",
                "has",
                "had",
                "have",
                "did",
                "does",
                "do",
                "done");
        AUXILIARY_VERBS = Collections.unmodifiableSet(builder);
    }

    /**
     * Returns true if this OrderedTokenSet potentially will match concepts. The case is sets of size 1
     * that are parts of speech that won't match concepts, or auxiliary verbs.
     * @return false if the token set can be trivially skipped, true otherwise
     */
    static boolean matchesConcepts(OrderedTokenSet orderedTokenSet) {
        List<Token> tokens = orderedTokenSet.getTokens();
        int size = tokens.size();
        if (size == 0) {
            return false;
        }
        if (size > 1) {
            return true;
        }
        Token token = tokens.get(0);
        PartOfSpeech partOfSpeech = token.getPartOfSpeech();
        return !(TRIVIAL_POS.contains(partOfSpeech) || (PartOfSpeech.VERB_CLASS.contains(partOfSpeech) && AUXILIARY_VERBS.contains(token.getNormalForm())));
    }

    /**
     * The maximum size of a term span.
     */
    static final int SPAN_SIZE = 8;

    /**
     * The concept dictionary to look up concepts from.
     */
    private final ConceptModel conceptDictionary;

    private final Document document;

    /**
     * Creates a dictionary concept recognizer from a concept dictionary and a document.
     *
     * @param conceptModel the dictionary to get concepts from.
     * @param document document to add new terms to.
     */
    @Inject
    DictionaryConceptRecognizer(ConceptModel conceptModel, Document document) {
        this.document = document;
        this.conceptDictionary = conceptModel;
    }

    /**
     * Finds the terms in a specific sentence, adding those terms to the document.
     *
     * @param sentence sentence to find terms in.
     */
    void identifyTermsInSentence(Sentence sentence) {
        LOGGER.trace("Identifying concepts in a sentence");
        TextOrderedTokenSet sentenceOrderedTokenSet = new SentenceTextOrderedTokenSet(sentence);

        sentenceOrderedTokenSet.orderedSubsetsSmallerThan(SPAN_SIZE)
                .filter(DictionaryConceptRecognizer::matchesConcepts)
                .map(conceptDictionary::findConcepts)
                .filter((@Nullable Term t) -> t != null)
                .forEach(document::addTerm);

        sentenceOrderedTokenSet.prepositionalSyntacticPermutationsStream()
                .filter((@Nullable OrderedTokenSet p) -> p != null)
                .map(conceptDictionary::findConcepts)
                .filter((@Nullable Term t) -> t != null)
                .forEach(document::addTerm);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            identifyTermsInSentence(sentence);
        }
    }
}
