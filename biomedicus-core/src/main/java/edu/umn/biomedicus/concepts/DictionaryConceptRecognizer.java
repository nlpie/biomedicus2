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
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.simple.SimpleTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.common.tokensets.OrderedTokenSet;
import edu.umn.biomedicus.common.tokensets.SentenceTextOrderedTokenSet;
import edu.umn.biomedicus.common.tokensets.TextOrderedTokenSet;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.*;
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
        Collections.addAll(builder, "in", "of", "at", "on");
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

    private final TermIndex normIndex;

    /**
     * Creates a dictionary concept recognizer from a concept dictionary and a document.
     *
     * @param conceptModel the dictionary to get concepts from.
     * @param document     document to add new terms to.
     */
    @Inject
    DictionaryConceptRecognizer(ConceptModel conceptModel, Document document, Vocabulary vocabulary) {
        this.document = document;
        this.conceptModel = conceptModel;
        this.normIndex = vocabulary.normIndex();
    }

    private void checkTokenSet(TextOrderedTokenSet tokenSet) {
        Span span = tokenSet.getSpan();
        String phrase = document.getText().substring(span.getBegin(), span.getEnd());
        SUI phraseSUI = conceptModel.forPhrase(phrase);

        if (phraseSUI != null) {
            makeTerm(span, phraseSUI, 1);
            return;
        }

        List<Token> tokens = tokenSet.getTokens();
        List<String> norms = tokens.stream()
                .map(Token::getNormalForm)
                .sorted()
                .collect(Collectors.toList());

        Optional<TermVector> lookup = normIndex.lookup(norms);
        if (lookup.isPresent()) {
            List<CUI> normsCUI = conceptModel.forNorms(lookup.get());
            if (normsCUI != null) {
                makeTerm(span, normsCUI, .6);
                return;
            }
        }

        List<Token> filtered = new ArrayList<>();
        if (tokens.size() > 2) {
            filtered.addAll(tokens.subList(1, tokens.size() - 1)
                    .stream()
                    .filter(token -> !TRIVIAL_POS.contains(token.getPartOfSpeech()))
                    .filter(token -> !AUXILIARY_VERBS.contains(token.getNormalForm()))
                    .filter(token -> !PREPOSITIONS.contains(token.getNormalForm()))
                    .collect(Collectors.toList()));
        }

        filtered.add(0, tokens.get(0));

        filtered.add(tokens.get(tokens.size() - 1));

        if (!filtered.equals(tokens)) {
            List<String> filteredNorms = filtered.stream()
                    .map(Token::getNormalForm)
                    .sorted()
                    .collect(Collectors.toList());

            Optional<TermVector> filterNormsLookup = normIndex.lookup(filteredNorms);
            if (filterNormsLookup.isPresent()) {
                List<CUI> filteredNormsCUI = conceptModel.forNorms(filterNormsLookup.get());
                if (filteredNormsCUI != null) {
                    makeTerm(span, filteredNormsCUI, .4);
                }
            }

        }
    }

    private void makeTerm(Span span, SUI sui, double confidence) {
        List<Concept> concepts = conceptModel.forSUI(sui).stream()
                .flatMap(cui -> conceptModel.termsForCUI(cui)
                        .stream()
                        .map(tui -> new UmlsConcept(cui, tui, sui, confidence))).collect(Collectors.toList());

        Term term = new SimpleTerm(span.getBegin(), span.getEnd(), concepts.get(0),
                concepts.subList(1, concepts.size()));
        document.addTerm(term);
    }

    private void makeTerm(Span span, List<CUI> cuis, double confidence) {
        List<Concept> concepts = cuis.stream().flatMap(cui -> conceptModel.termsForCUI(cui)
                .stream()
                .map(tui -> new UmlsConcept(cui, tui, null, confidence))).collect(Collectors.toList());

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

            sentenceOrderedTokenSet.orderedSubsetsSmallerThan(SPAN_SIZE)
                    .filter(DictionaryConceptRecognizer::matchesConcepts)
                    .forEach(this::checkTokenSet);
        }
    }
}
