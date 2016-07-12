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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.Module;
import edu.umn.biomedicus.common.semantics.*;
import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.plugins.AbstractPlugin;
import edu.umn.biomedicus.type.ConceptAnnotation;
import edu.umn.biomedicus.type.SentenceAnnotation;
import edu.umn.biomedicus.type.TermAnnotation;
import org.apache.uima.jcas.cas.FSArray;

import java.util.Arrays;
import java.util.Collection;

public class BiomedicusTsLabelsPlugin extends AbstractPlugin {

    @Override
    public Collection<? extends Module> modules() {
        return Arrays.asList(
                new LabelableModule<Sentence2, SentenceAnnotation>(Sentence2.class) {
                    @Override
                    protected LabelAdapter<Sentence2, SentenceAnnotation> getLabelAdapter() {
                        return LabelAdapter.builder(Sentence2.class)
                                .withAnnotationClass(SentenceAnnotation.class)
                                .withLabelableAdapter((sentence2, sentenceAnnotation) -> {
                                })
                                .withAnnotationAdapter(sentenceAnnotation -> new Sentence2());
                    }
                },
                new LabelableModule<DictionaryTerm, TermAnnotation>(DictionaryTerm.class) {
                    @Override
                    protected LabelAdapter<DictionaryTerm, TermAnnotation> getLabelAdapter() {
                        return LabelAdapter.builder(DictionaryTerm.class)
                                .withAnnotationClass(TermAnnotation.class)
                                .withLabelableAdapter((dictionaryTerm, termAnnotation) -> {
                                    throw new UnsupportedOperationException("Not supported");
                                })
                                .withAnnotationAdapter(termAnnotation -> {
                                    ConceptAnnotation primaryConcept = termAnnotation.getPrimaryConcept();
                                    DictionaryConcept dictionaryConcept = DictionaryConcept.builder()
                                            .withIdentifier(primaryConcept.getIdentifier())
                                            .withSource(primaryConcept.getSource())
                                            .withType(primaryConcept.getSemanticType())
                                            .withConfidence(primaryConcept.getConfidence())
                                            .build();
                                    DictionaryTerm.Builder builder = DictionaryTerm.builder().addConcept(dictionaryConcept);
                                    FSArray alternativeConcepts = termAnnotation.getAlternativeConcepts();
                                    for (int i = 0; i < alternativeConcepts.size(); i++) {
                                        ConceptAnnotation alternativeConcept = termAnnotation.getAlternativeConcepts(i);
                                        DictionaryConcept altDictConcept = DictionaryConcept.builder()
                                                .withIdentifier(alternativeConcept.getIdentifier())
                                                .withSource(alternativeConcept.getSource())
                                                .withType(alternativeConcept.getSemanticType())
                                                .withConfidence(alternativeConcept.getConfidence())
                                                .build();
                                        builder.addConcept(altDictConcept);
                                    }

                                    return builder.build();
                                });
                    }
                },
                new LabelableModule<ParseToken, edu.umn.biomedicus.uima.type1_5.ParseToken>(ParseToken.class) {
                    @Override
                    public LabelAdapter<ParseToken, edu.umn.biomedicus.uima.type1_5.ParseToken> getLabelAdapter() {
                        return LabelAdapter.builder(ParseToken.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.ParseToken.class)
                                .withLabelableAdapter((parseToken, annotation) -> {
                                    annotation.setText(parseToken.getText());
                                    annotation.setTrailingText(parseToken.getTrailingText());
                                })
                                .withAnnotationAdapter(annotation -> new ParseToken(annotation.getText(), annotation.getTrailingText()));
                    }
                },
                new LabelableModule<PartOfSpeech, edu.umn.biomedicus.uima.type1_5.ParseToken>(PartOfSpeech.class) {
                    @Override
                    public LabelAdapter<PartOfSpeech, edu.umn.biomedicus.uima.type1_5.ParseToken> getLabelAdapter() {
                        return LabelAdapter.builder(PartOfSpeech.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.ParseToken.class)
                                .withLabelableAdapter((parseToken, annotation) -> {
                                    throw new UnsupportedOperationException("Creating part of speech labels using labeler currently unsupported.");
                                })
                                .withAnnotationAdapter(annotation -> PartsOfSpeech.forTag(annotation.getPartOfSpeech()));
                    }
                },
                new LabelableModule<TermToken, edu.umn.biomedicus.uima.type1_5.TermToken>(TermToken.class) {
                    @Override
                    public LabelAdapter<TermToken, edu.umn.biomedicus.uima.type1_5.TermToken> getLabelAdapter() {
                        return LabelAdapter.builder(TermToken.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.TermToken.class)
                                .withLabelableAdapter((termToken, annotation) -> {
                                    annotation.setText(termToken.getText());
                                    annotation.setTrailingText(termToken.getTrailingText());
                                })
                                .withAnnotationAdapter(annotation -> new TermToken(annotation.getText(), annotation.getTrailingText()));
                    }
                },
                new LabelableModule<Acronym, edu.umn.biomedicus.uima.type1_5.Acronym>(Acronym.class) {
                    @Override
                    protected LabelAdapter<Acronym, edu.umn.biomedicus.uima.type1_5.Acronym> getLabelAdapter() {
                        return LabelAdapter.builder(Acronym.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.Acronym.class)
                                .withLabelableAdapter((acronym, acronym2) -> {
                                    // acronym has no properties
                                })
                                .withAnnotationAdapter(acronym -> new Acronym());
                    }
                },
                new LabelableModule<AcronymExpansion, edu.umn.biomedicus.uima.type1_5.AcronymExpansion>(AcronymExpansion.class) {
                    @Override
                    protected LabelAdapter<AcronymExpansion, edu.umn.biomedicus.uima.type1_5.AcronymExpansion> getLabelAdapter() {
                        return LabelAdapter.builder(AcronymExpansion.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.AcronymExpansion.class)
                                .withLabelableAdapter((acronymExpansion, annotation) -> {
                                    annotation.setLongform(acronymExpansion.getText());
                                    annotation.setTrailingText(acronymExpansion.getTrailingText());
                                })
                                .withAnnotationAdapter(annotation -> new AcronymExpansion(annotation.getLongform(), annotation.getTrailingText()));
                    }
                },
                new LabelableModule<WordIndex, edu.umn.biomedicus.uima.type1_5.WordIndex>(WordIndex.class) {
                    @Override
                    protected LabelAdapter<WordIndex, edu.umn.biomedicus.uima.type1_5.WordIndex> getLabelAdapter() {
                        return LabelAdapter.builder(WordIndex.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.WordIndex.class)
                                .withLabelableAdapter((wordIndex, annotation) -> {
                                    annotation.setIndex(wordIndex.term().indexedTerm());
                                })
                                .withAnnotationAdapter(annotation -> new WordIndex(new IndexedTerm(annotation.getIndex())));
                    }
                },
                new LabelableModule<NormIndex, edu.umn.biomedicus.uima.type1_5.NormIndex>(NormIndex.class) {
                    @Override
                    protected LabelAdapter<NormIndex, edu.umn.biomedicus.uima.type1_5.NormIndex> getLabelAdapter() {
                        return LabelAdapter.builder(NormIndex.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.NormIndex.class)
                                .withLabelableAdapter((normIndex, annotation) -> {
                                    annotation.setIndex(normIndex.term().indexedTerm());
                                })
                                .withAnnotationAdapter(annotation -> new NormIndex(new IndexedTerm(annotation.getIndex())));
                    }
                },
                new LabelableModule<Historical, edu.umn.biomedicus.uima.type1_5.Historical>(Historical.class) {
                    @Override
                    protected LabelAdapter<Historical, edu.umn.biomedicus.uima.type1_5.Historical> getLabelAdapter() {
                        return LabelAdapter.builder(Historical.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.Historical.class)
                                .withLabelableAdapter((historical, annotation) -> {})
                                .withAnnotationAdapter(annotation -> new Historical());
                    }
                },
                new LabelableModule<Negated, edu.umn.biomedicus.uima.type1_5.Negated>(Negated.class) {
                    @Override
                    protected LabelAdapter<Negated, edu.umn.biomedicus.uima.type1_5.Negated> getLabelAdapter() {
                        return LabelAdapter.builder(Negated.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.Negated.class)
                                .withLabelableAdapter((negated, negated2) -> {})
                                .withAnnotationAdapter(negated -> new Negated());
                    }
                },
                new LabelableModule<Probable, edu.umn.biomedicus.uima.type1_5.Probable>(Probable.class) {
                    @Override
                    protected LabelAdapter<Probable, edu.umn.biomedicus.uima.type1_5.Probable> getLabelAdapter() {
                        return LabelAdapter.builder(Probable.class)
                                .withAnnotationClass(edu.umn.biomedicus.uima.type1_5.Probable.class)
                                .withLabelableAdapter((probable, probable2) -> {})
                                .withAnnotationAdapter(probable -> new Probable());
                    }
                }
        );
    }
}
