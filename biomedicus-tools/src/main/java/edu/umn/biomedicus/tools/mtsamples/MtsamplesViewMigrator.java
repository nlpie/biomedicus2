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

package edu.umn.biomedicus.tools.mtsamples;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.type.*;
import edu.umn.biomedicus.uima.copying.ViewMigrator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Converts XMI stored in the old MTSamples format to the current biomedicus type system.
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public class MtsamplesViewMigrator implements ViewMigrator {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MtsamplesViewMigrator.class);

    /**
     * A pattern for space.
     */
    private static final Pattern space = Pattern.compile(" ");

    @Override
    public void migrate(JCas source, JCas target) {
        target.setDocumentText(source.getDocumentText());

        TypeSystem oldTypeSystem = source.getTypeSystem();
        Type oldDocumentType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Document");
        AnnotationIndex<Annotation> documentIndex = source.getAnnotationIndex(oldDocumentType);
        Feature documentTypeIdFeature = oldDocumentType.getFeatureByBaseName("typeId");
        Feature documentSampleIdFeature = oldDocumentType.getFeatureByBaseName("sampleId");
        Feature documentSampleNameFeature = oldDocumentType.getFeatureByBaseName("sampleName");
        for (Annotation annotation : documentIndex) {
            ClinicalNoteAnnotation clinicalNoteAnnotation = new ClinicalNoteAnnotation(target,
                    annotation.getBegin(), annotation.getEnd());
            clinicalNoteAnnotation.setDocumentId(annotation.getStringValue(documentTypeIdFeature) + "_" + annotation.getStringValue(documentSampleIdFeature));
            clinicalNoteAnnotation.setCategory(annotation.getStringValue(documentSampleNameFeature));
            clinicalNoteAnnotation.addToIndexes();
        }

        Type sectionType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Section");
        Feature sectionTitle = sectionType.getFeatureByBaseName("sectionTitle");
        AnnotationIndex<Annotation> sectionIndex = source.getAnnotationIndex(sectionType);
        for (Annotation annotation : sectionIndex) {
            SectionAnnotation sectionAnnotation = new SectionAnnotation(target, annotation.getBegin(),
                    annotation.getEnd());
            sectionAnnotation.setSectionTitle(annotation.getStringValue(sectionTitle));
            sectionAnnotation.addToIndexes();
        }

        Type tokenType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Token");
        Feature posFeature = tokenType.getFeatureByBaseName("tokenPOS");
        Feature normFormFeature = tokenType.getFeatureByBaseName("normForm");

        AnnotationIndex<Annotation> tokenIndex = source.getAnnotationIndex(tokenType);
        for (Annotation annotation : tokenIndex) {
            TokenAnnotation tokenAnnotation = new TokenAnnotation(target, annotation.getBegin(), annotation.getEnd());
            String tokenPOS = annotation.getStringValue(posFeature).toUpperCase().trim();
            if (tokenPOS.equals(";")) {
                tokenPOS = ":";
            }
            if (tokenPOS.equals("?")) {
                tokenPOS = ".";
            }
            PartOfSpeech partOfSpeech = PartOfSpeech.MAP.get(tokenPOS);
            if (partOfSpeech == null) {
                partOfSpeech = PartOfSpeech.FALLBACK_MAP.get(tokenPOS);
                if (partOfSpeech == null) {
                    LOGGER.error("Unrecognized part of speech {}", tokenPOS);
                    throw new RuntimeException();
                }
            }
            tokenAnnotation.setPartOfSpeech(partOfSpeech.toString());
            tokenAnnotation.setNormalForm(annotation.getStringValue(normFormFeature));
            tokenAnnotation.addToIndexes();
        }

        Type sentenceType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Sentence");
        Feature isHeadingFeature = sentenceType.getFeatureByBaseName("isHeading");

        AnnotationIndex<Annotation> sentenceIndex = source.getAnnotationIndex(sentenceType);
        for (Annotation annotation : sentenceIndex) {
            SentenceAnnotation sentenceAnnotation = new SentenceAnnotation(target, annotation.getBegin(), annotation.getEnd());
            sentenceAnnotation.setIsHeading(annotation.getBooleanValue(isHeadingFeature));
            sentenceAnnotation.addToIndexes();
        }

        Type termType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Term");
        Feature termAspectFeature = termType.getFeatureByBaseName("termAspect");
        Feature termAttributionFeature = termType.getFeatureByBaseName("termAttribution");
        Feature termCertaintyFeature = termType.getFeatureByBaseName("termCertainty");
        Feature isAcronymFeature = termType.getFeatureByBaseName("isAcronym");
        Feature termMeaningFeature = termType.getFeatureByBaseName("termMeaning");
        Feature termNegationFeature = termType.getFeatureByBaseName("termNegation");
        Feature termConceptFeature = termType.getFeatureByBaseName("termConcept");

        Type conceptType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Concept");
        Feature conceptIdFeature = conceptType.getFeatureByBaseName("conceptId");
        Feature conceptTypeFeature = conceptType.getFeatureByBaseName("conceptType");
        Feature conceptSourceFeature = conceptType.getFeatureByBaseName("conceptSource");
        Feature conceptConfidenceFeature = conceptType.getFeatureByBaseName("conceptConfidence");

        AnnotationIndex<Annotation> termIndex = source.getAnnotationIndex(termType);
        for (Annotation annotation : termIndex) {
            TermAnnotation termAnnotation = new TermAnnotation(target, annotation.getBegin(), annotation.getEnd());
            termAnnotation.setAspect(annotation.getStringValue(termAspectFeature));
            termAnnotation.setAttribution(annotation.getStringValue(termAttributionFeature));
            termAnnotation.setCertainty(annotation.getStringValue(termCertaintyFeature));
            termAnnotation.setIsAcronym(annotation.getBooleanValue(isAcronymFeature));
            termAnnotation.setMeaning(annotation.getStringValue(termMeaningFeature));
            termAnnotation.setIsNegated(annotation.getBooleanValue(termNegationFeature));

            FeatureStructure termConcept = annotation.getFeatureValue(termConceptFeature);
            ConceptAnnotation primaryConcept = null;
            FSArray fsArray = null;
            if (termConcept == null) {
                String meaning = termAnnotation.getMeaning();
                if (meaning != null && meaning.matches("C[0-9]{7}+")) {
                    ConceptAnnotation conceptAnnotation = new ConceptAnnotation(target, annotation.getBegin(), annotation.getEnd());
                    conceptAnnotation.setIdentifier(meaning);
                    conceptAnnotation.addToIndexes();
                    primaryConcept = conceptAnnotation;
                } else {
                    LOGGER.warn("Term concept was null: {}", meaning);
                    continue;
                }
            } else {
                String termConceptsValue = termConcept.getStringValue(conceptIdFeature);
                String[] concepts = space.split(termConceptsValue);
                String[] types = space.split(termConcept.getStringValue(conceptTypeFeature));
                int length = concepts.length - 1;
                fsArray = new FSArray(target, length);
                for (int i = 0; i < length; i++) {
                    ConceptAnnotation conceptAnnotation = new ConceptAnnotation(target, annotation.getBegin(), annotation.getEnd());
                    conceptAnnotation.setIdentifier(concepts[i]);
                    if (types.length <= i) {
                        conceptAnnotation.setSemanticType(types[0]);
                    } else {
                        conceptAnnotation.setSemanticType(types[i]);
                    }
                    conceptAnnotation.setSource(termConcept.getStringValue(conceptSourceFeature));
                    conceptAnnotation.setConfidence(termConcept.getFloatValue(conceptConfidenceFeature));
                    conceptAnnotation.addToIndexes();
                    if (i == 0) {
                        primaryConcept = conceptAnnotation;
                    } else {
                        fsArray.set(i - 1, conceptAnnotation);
                    }
                }
            }
            termAnnotation.setPrimaryConcept(primaryConcept);
            termAnnotation.setAlternativeConcepts(fsArray);
            termAnnotation.addToIndexes();
        }
    }
}
