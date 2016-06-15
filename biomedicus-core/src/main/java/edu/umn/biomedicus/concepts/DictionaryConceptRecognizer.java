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

package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.collect.DistinctSpansMap;
import edu.umn.biomedicus.common.collect.SlidingWindow;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.semantics.PartsOfSpeech;
import edu.umn.biomedicus.common.simple.SimpleTerm;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.common.texttools.PhraseEditor;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryConceptRecognizer.class);

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
                PartsOfSpeech.PUNCTUATION_CLASS.toArray(new PartOfSpeech[PartsOfSpeech.PUNCTUATION_CLASS.size()]));
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
     * The maximum size of a term span.
     */
    private static final int SPAN_SIZE = 8;

    /**
     * The concept dictionary to look up concepts from.
     */
    private final ConceptModel conceptModel;
    private final Labels<Sentence2> sentences;
    private final Labels<NormIndex> normIndexes;
    private final Labels<TermToken> termTokens;
    private final Labels<PartOfSpeech> partsOfSpeech;
    private final Document document;
    private final Labels<AcronymExpansion> acronymExpansions;
    private String text;

    /**
     * Creates a dictionary concept recognizer from a concept dictionary and a document.
     *
     * @param conceptModel the dictionary to get concepts from.
     */
    @Inject
    DictionaryConceptRecognizer(ConceptModel conceptModel,
                                Document document,
                                Labels<Sentence2> sentences,
                                Labels<AcronymExpansion> acronymExpansions,
                                Labels<NormIndex> normIndexes,
                                Labels<TermToken> termTokens,
                                Labels<PartOfSpeech> partsOfSpeech) {
        this.document = document;
        this.conceptModel = conceptModel;
        this.sentences = sentences;
        this.acronymExpansions = acronymExpansions;
        this.normIndexes = normIndexes;
        this.termTokens = termTokens;
        this.partsOfSpeech = partsOfSpeech;
    }

    private boolean checkPhrase(Span span, String phrase, boolean oneToken) throws BiomedicusException {
        List<SuiCuiTui> phraseSUI = conceptModel.forPhrase(phrase);

        if (phraseSUI != null) {
            makeTerm(span, phraseSUI, 1);
            return true;
        }

        if (oneToken) {
            return false;
        }

        phraseSUI = conceptModel.forLowercasePhrase(phrase.toLowerCase());

        if (phraseSUI != null) {
            makeTerm(span, phraseSUI, 0.6);
            return true;
        }

        return false;
    }

    private void checkTokenSet(List<Label<TermToken>> tokenSet) {
        if (tokenSet.size() <= 1) {
            return;
        }

        Span phraseAsSpan = new Span(tokenSet.get(0).getBegin(), tokenSet.get(tokenSet.size() - 1).getEnd());
        TermsBag.Builder builder = TermsBag.builder();
        for (Label<NormIndex> normIndexLabel : normIndexes.insideSpan(phraseAsSpan)) {
            builder.addTerm(normIndexLabel.value().term());
        }
        TermsBag normVector = builder.build();

        List<SuiCuiTui> normsCUI = conceptModel.forNorms(normVector);
        if (normsCUI != null) {
            makeTerm(phraseAsSpan, normsCUI, .3);
        }
    }

    private void makeTerm(SpanLike spanLike, List<SuiCuiTui> cuis, double confidence) {
        List<Concept> concepts = cuis.stream()
                .map(suiCuiTui -> suiCuiTui.toConcept(confidence)).collect(Collectors.toList());

        Term term = new SimpleTerm(spanLike.getBegin(), spanLike.getEnd(), concepts.get(0),
                concepts.subList(1, concepts.size()));
        document.addTerm(term);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Finding concepts in document.");
        text = document.getText();
        for (Label<Sentence2> sentence : sentences) {
            LOGGER.trace("Identifying concepts in a sentence");

            StringBuilder editedString = new StringBuilder();
            List<Span> editedStringSpans = new ArrayList<>();
            List<Label<TermToken>> sentenceTermTokens = termTokens.insideSpan(sentence).all();
            for (Label<TermToken> sentenceTermToken : sentenceTermTokens) {
                Optional<Label<AcronymExpansion>> acronymForToken = acronymExpansions.withSpan(sentenceTermToken);
                TokenLike tokenLike;
                if (acronymForToken.isPresent()) {
                    tokenLike = acronymForToken.get().value();
                } else {
                    tokenLike = sentenceTermToken.value();
                }
                String tokenText = tokenLike.getText();
                String trailingText = tokenLike.getTrailingText();
                Span span = new Span(editedString.length(), editedString.length() + tokenText.length());
                editedString.append(tokenLike);
                editedString.append(trailingText);
                editedStringSpans.add(span);
            }

            for (int i = 0; i < sentenceTermTokens.size() - SPAN_SIZE; i++) {
                List<Label<TermToken>> window = sentenceTermTokens.subList(i, i + SPAN_SIZE);
                for (int subsetSize = 1; subsetSize < window.size(); subsetSize++) {
                    List<Label<TermToken>> windowSubset = window.subList(0, subsetSize);
                    Span asSpan = new Span(windowSubset.get(0).getBegin(), windowSubset.get(subsetSize - 1).getEnd());
                    boolean phraseFound = checkPhrase(asSpan, text.substring(asSpan.getBegin(), asSpan.getEnd()),
                            subsetSize == 1);
                    if (!phraseFound) {
                        int editedBegin = editedStringSpans.get(i).getBegin();
                        int editedEnd = editedStringSpans.get(i + subsetSize).getEnd();
                        String editedSubstring = editedString.substring(editedBegin, editedEnd);
                        checkPhrase(asSpan, editedSubstring, subsetSize == 1);
                    }
                    if (!phraseFound) {
                        if (matchesConcepts(windowSubset.get(0)) && (matchesConcepts(windowSubset.get(subsetSize)))) {
                            checkTokenSet(windowSubset);
                        }
                    }
                }
            }
        }
    }

    private boolean matchesConcepts(SpanLike token) {
        String text = token.getCovered(this.text).toString();
        List<Label<NormIndex>> normsForToken = normIndexes.insideSpan(token).all();

        return !PREPOSITIONS.contains(text) && !AUXILIARY_VERBS.contains(text)
                && partsOfSpeech.insideSpan(token).stream().allMatch(TRIVIAL_POS::contains)
                && normsForToken.stream().allMatch(l -> l.value().term().isUnknown())
                && Patterns.A_LETTER_OR_NUMBER.matcher(text).find();
    }
}
