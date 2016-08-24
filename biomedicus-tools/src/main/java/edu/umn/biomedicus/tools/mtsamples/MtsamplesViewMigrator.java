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

package edu.umn.biomedicus.tools.mtsamples;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.uima.copying.ViewMigrator;
import edu.umn.biomedicus.uima.type1_5.*;
import edu.umn.biomedicus.uima.type1_6.*;
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

import java.util.Optional;
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
            DocumentId clinicalNoteAnnotation = new DocumentId(target);
            clinicalNoteAnnotation.setDocumentId(annotation.getStringValue(documentTypeIdFeature) + "_" + annotation.getStringValue(documentSampleIdFeature));
            clinicalNoteAnnotation.addToIndexes();

            DocumentMetadata documentMetadata = new DocumentMetadata(target);
            documentMetadata.setKey("category");
            documentMetadata.setValue(annotation.getStringValue(documentSampleNameFeature));
            documentMetadata.addToIndexes();
        }

        Type sectionType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Section");
        Feature sectionTitle = sectionType.getFeatureByBaseName("sectionTitle");
        AnnotationIndex<Annotation> sectionIndex = source.getAnnotationIndex(sectionType);
        for (Annotation annotation : sectionIndex) {
            Section sectionAnnotation = new Section(target, annotation.getBegin(), annotation.getEnd());
            sectionAnnotation.addToIndexes();
            SectionContent sectionContent = new SectionContent(target, annotation.getBegin(), annotation.getEnd());
            sectionContent.addToIndexes();
        }

        Type tokenType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Token");
        Feature posFeature = tokenType.getFeatureByBaseName("tokenPOS");
        Feature normFormFeature = tokenType.getFeatureByBaseName("normForm");

        AnnotationIndex<Annotation> tokenIndex = source.getAnnotationIndex(tokenType);
        for (Annotation annotation : tokenIndex) {
            ParseToken tokenAnnotation = new ParseToken(target, annotation.getBegin(), annotation.getEnd());
            String tokenPOS = annotation.getStringValue(posFeature).toUpperCase().trim();
            if (tokenPOS.equals(";")) {
                tokenPOS = ":";
            }
            if (tokenPOS.equals("?")) {
                tokenPOS = ".";
            }
            Optional<PartOfSpeech> partOfSpeech = PartsOfSpeech.forTagWithFallback(tokenPOS);
            if (!partOfSpeech.isPresent()) {
                LOGGER.error("Unrecognized part of speech {}", tokenPOS);
                throw new RuntimeException();
            }
            tokenAnnotation.addToIndexes();

            PartOfSpeechTag partOfSpeechTag = new PartOfSpeechTag(target, annotation.getBegin(), annotation.getEnd());
            partOfSpeechTag.setPartOfSpeech(tokenPOS);
            partOfSpeechTag.addToIndexes();
        }

        Type sentenceType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Sentence");

        AnnotationIndex<Annotation> sentenceIndex = source.getAnnotationIndex(sentenceType);
        for (Annotation annotation : sentenceIndex) {
            Sentence sentenceAnnotation = new Sentence(target, annotation.getBegin(), annotation.getEnd());
            sentenceAnnotation.addToIndexes();
        }

        Type termType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Term");
        Feature termAspectFeature = termType.getFeatureByBaseName("termAspect");
        Feature termCertaintyFeature = termType.getFeatureByBaseName("termCertainty");
        Feature termNegationFeature = termType.getFeatureByBaseName("termNegation");
        Feature termConceptFeature = termType.getFeatureByBaseName("termConcept");

        Type conceptType = oldTypeSystem.getType("edu.umn.biomedicus.mtsamples.types.Concept");
        Feature conceptIdFeature = conceptType.getFeatureByBaseName("conceptId");
        Feature conceptTypeFeature = conceptType.getFeatureByBaseName("conceptType");
        Feature conceptSourceFeature = conceptType.getFeatureByBaseName("conceptSource");
        Feature conceptConfidenceFeature = conceptType.getFeatureByBaseName("conceptConfidence");

        AnnotationIndex<Annotation> termIndex = source.getAnnotationIndex(termType);
        for (Annotation annotation : termIndex) {
            DictionaryTerm termAnnotation = new DictionaryTerm(target, annotation.getBegin(), annotation.getEnd());

            if ("past".equals(annotation.getStringValue(termAspectFeature))) {
                Historical historical = new Historical(target, annotation.getBegin(), annotation.getEnd());
                historical.addToIndexes();
            }

            if ("probable".equals(annotation.getStringValue(termCertaintyFeature))) {
                Probable probable = new Probable(target, annotation.getBegin(), annotation.getEnd());
                probable.addToIndexes();
            }

            if (annotation.getBooleanValue(termNegationFeature)) {
                Negated negated = new Negated(target, annotation.getBegin(), annotation.getEnd());
                negated.addToIndexes();
            }

            FeatureStructure termConcept = annotation.getFeatureValue(termConceptFeature);

            String termConceptsValue = termConcept.getStringValue(conceptIdFeature);
            String[] concepts = space.split(termConceptsValue);
            String[] types = space.split(termConcept.getStringValue(conceptTypeFeature));
            int length = concepts.length;
            FSArray fsArray = new FSArray(target, length);
            for (int i = 0; i < length; i++) {
                DictionaryConcept concept = new DictionaryConcept(target, annotation.getBegin(), annotation.getEnd());
                concept.setIdentifier(concepts[i]);
                if (types.length <= i) {
                    concept.setSemanticType(types[0]);
                } else {
                    concept.setSemanticType(types[i]);
                }
                concept.setSource(termConcept.getStringValue(conceptSourceFeature));
                concept.setConfidence(termConcept.getFloatValue(conceptConfidenceFeature));
                concept.addToIndexes();
                fsArray.set(i, concept);
            }
            fsArray.addToIndexes();
            termAnnotation.setConcepts(fsArray);
            termAnnotation.addToIndexes();
        }
    }
}
