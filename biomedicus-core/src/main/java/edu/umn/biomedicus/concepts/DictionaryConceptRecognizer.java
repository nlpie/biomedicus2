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

import edu.umn.biomedicus.acronym.AcronymVectorModel;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.simple.SimpleTerm;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.common.tokensets.OrderedTokenSet;
import edu.umn.biomedicus.common.tokensets.SentenceTextOrderedTokenSet;
import edu.umn.biomedicus.common.tokensets.TextOrderedTokenSet;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.umn.biomedicus.common.semantics.PartOfSpeech.*;

/**
 * Uses a {@link edu.umn.biomedicus.concepts.ConceptModel} to recognize concepts in text. First, it will
 * try to find direct matches against all in-order sublists of tokens in a sentence. Then it will perform syntactic
 * permutations on any prepositional phrases in those sublists.
 *
 * @author Ben Knoll
 * @author Serguei Pakhomov
 * @since 1.0.0
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
                IN,
                XX);
        Collections.addAll(builder,
                PartOfSpeech.PUNCTUATION_CLASS.toArray(new PartOfSpeech[PartOfSpeech.PUNCTUATION_CLASS.size()]));
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
     * A set of English prepositions.
     */
    private static final Set<String> PREPOSITIONS;

    static {
        Set<String> builder = new HashSet<>();
        Collections.addAll(builder, "in", "of", "at", "on", "under");
        PREPOSITIONS = Collections.unmodifiableSet(builder);
    }

    /**
     * Returns true if this OrderedTokenSet potentially will match concepts. The case is sets of size 1
     * that are parts of speech that won't match concepts, or auxiliary verbs.
     *
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
    private final ConceptModel conceptModel;

    private final Document document;

    /**
     * Creates a dictionary concept recognizer from a concept dictionary and a document.
     *
     * @param conceptModel the dictionary to get concepts from.
     * @param document     document to add new terms to.
     */
    @Inject
    DictionaryConceptRecognizer(ConceptModel conceptModel, Document document) {
        this.document = document;
        this.conceptModel = conceptModel;
    }

    private boolean checkPhrase(TextOrderedTokenSet tokenSet) {
        Span span = tokenSet.getSpan();

        List<Token> tokens = tokenSet.getTokens();
        int spanBegin = span.getBegin();
        String phrase = document.getText().substring(spanBegin, span.getEnd());
        List<SuiCuiTui> phraseSUI = conceptModel.forPhrase(phrase);
        if (phraseSUI == null) {
            StringBuilder phraseEdited = new StringBuilder(phrase);

            int offset = 0;
            for (Token token : tokens) {
                String replacement = null;
                if (token.isAcronym()) {
                    replacement = token.getLongForm();
                } else if (token.isMisspelled()) {
                    replacement = token.correctSpelling();
                }
                if (replacement != null && !AcronymVectorModel.UNK.equals(replacement)) {
                    int tokenBegin = token.getBegin();
                    int beginOffset = tokenBegin - spanBegin + offset;
                    int tokenEnd = token.getEnd();
                    int endOffset = tokenEnd - spanBegin + offset;
                    offset += replacement.length() - (tokenEnd - tokenBegin);
                    phraseEdited.replace(beginOffset, endOffset, replacement);
                }
            }
            phrase = phraseEdited.toString();
        }

        phraseSUI = conceptModel.forPhrase(phrase);

        if (phraseSUI != null) {
            makeTerm(span, phraseSUI, 1);
            return true;
        }

        if (tokenSet.size() <= 1) {
            return false;
        }

        phraseSUI = conceptModel.forLowercasePhrase(phrase.toLowerCase());

        if (phraseSUI != null) {
            makeTerm(span, phraseSUI, 0.6);
            return true;
        }
        return false;
    }

    private void checkTokenSet(TextOrderedTokenSet tokenSet) {
        if (tokenSet.size() <= 1) {
            return;
        }

        Span span = tokenSet.getSpan();
        TermsBag normVector = tokenSet.getNormVector();

        List<SuiCuiTui> normsCUI = conceptModel.forNorms(normVector);
        if (normsCUI != null) {
            makeTerm(span, normsCUI, .3);
        }
    }

    private void makeTerm(Span span, List<SuiCuiTui> cuis, double confidence) {
        List<Concept> concepts = cuis.stream()
                .map(suiCuiTui -> suiCuiTui.toConcept(confidence)).collect(Collectors.toList());

        Term term = new SimpleTerm(span.getBegin(), span.getEnd(), concepts.get(0),
                concepts.subList(1, concepts.size()));
        document.addTerm(term);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Finding concepts in document.");
        for (Sentence sentence : document.getSentences()) {
            LOGGER.trace("Identifying concepts in a sentence");

            TextOrderedTokenSet sentenceOrderedTokenSet = new SentenceTextOrderedTokenSet(sentence);

            sentenceOrderedTokenSet.orderedSubsetsSmallerThan(SPAN_SIZE).forEach(textOrderedTokenSet -> {
                boolean phraseFound = checkPhrase(textOrderedTokenSet);
                if (!phraseFound) {
                    if (matchesConcepts(textOrderedTokenSet.firstToken()) && matchesConcepts(textOrderedTokenSet.lastToken())) {
                        checkTokenSet(textOrderedTokenSet.filter(this::matchesConcepts));
                    }
                }
            });
        }
    }

    boolean matchesConcepts(Token token) {
        String text = token.getText();
        return !PREPOSITIONS.contains(text) && !AUXILIARY_VERBS.contains(text)
                && !TRIVIAL_POS.contains(token.getPartOfSpeech()) && !token.getNormTerm().isUnknown()
                && Patterns.A_LETTER_OR_NUMBER.matcher(text).find();
    }
}
