/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.migration;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.concepts.CUI;
import edu.umn.biomedicus.uima.adapter.CASDocument;
import edu.umn.biomedicus.uima.util.FsAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

/**
 * Converts XMIs in the MTSamples type system to XMIs in the 1_7_0 type system.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class MtsamplesTo1_7_0 implements TypeSystemMigration {

  @Override
  public List<TypeConversion> getTypeConversions() {
    ArrayList<TypeConversion> list = new ArrayList<>();

    list.add(new TypeConversion() {
      @Override
      public String sourceTypeName() {
        return "edu.umn.biomedicus.mtsamples.types.Section";
      }

      @Override
      public List<FeatureStructure> doMigrate(CAS sourceView, CAS targetView,
          FeatureStructure from,
          Type fromType) {
        AnnotationFS fromAnnotation = (AnnotationFS) from;
        TypeSystem typeSystem = sourceView.getTypeSystem();
        Type sectionType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.Section");
        int begin = fromAnnotation.getBegin();
        int end = fromAnnotation.getEnd();
        AnnotationFS to = targetView.createAnnotation(sectionType, begin, end);
        targetView.addFsToIndexes(to);

        String documentText = sourceView.getDocumentText();

        Feature sectionTitleFeat = fromType.getFeatureByBaseName("sectionTitle");
        String sectionTitle = from.getStringValue(sectionTitleFeat);

        String sectionText = documentText.substring(begin, end);
        int titleBegin = sectionText.indexOf(sectionTitle);
        int titleEnd = titleBegin + sectionTitle.length();

        Type sectionTitleType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.SectionTitle");

        int titleEndAbsolute = begin + titleEnd;

        AnnotationFS titleAnnotation = targetView
            .createAnnotation(sectionTitleType, begin + titleBegin, titleEndAbsolute);
        targetView.addFsToIndexes(titleAnnotation);

        Type sectionContentType = typeSystem
            .getType("edu.umn.biomedicus.uima.type1_6.SectionContent");

        AnnotationFS contentAnnotation = targetView
            .createAnnotation(sectionContentType, titleEndAbsolute, fromAnnotation.getEnd());
        targetView.addFsToIndexes(contentAnnotation);

        return Arrays.asList(to, titleAnnotation, contentAnnotation);
      }
    });

    list.add(new TypeConversion() {
      @Override
      public String sourceTypeName() {
        return "edu.umn.biomedicus.mtsamples.types.Token";
      }

      @Override
      public List<FeatureStructure> doMigrate(CAS sourceView, CAS targetView, FeatureStructure from,
          Type fromType) {
        TypeSystem typeSystem = sourceView.getTypeSystem();
        Type parseTokenType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.ParseToken");

        AnnotationFS fromAnnotation = ((AnnotationFS) from);

        int begin = fromAnnotation.getBegin();
        int end = fromAnnotation.getEnd();
        AnnotationFS parseToken = targetView.createAnnotation(parseTokenType, begin, end);

        String documentText = sourceView.getDocumentText();
        parseToken
            .setStringValue(parseTokenType.getFeatureByBaseName("text"),
                documentText.substring(begin, end));

        boolean whitespace = end < documentText.length()
            && Character.isWhitespace(documentText.charAt(end));
        parseToken.setBooleanValue(parseTokenType.getFeatureByBaseName("hasSpaceAfter"),
            whitespace);

        targetView.addFsToIndexes(parseToken);

        Type normFormType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.NormForm");
        Feature normFormFeature = normFormType.getFeatureByBaseName("normForm");

        AnnotationFS normFormAnnotation = targetView.createAnnotation(normFormType, begin, end);
        normFormAnnotation.setStringValue(normFormFeature,
            fromAnnotation.getStringValue(fromType.getFeatureByBaseName("normForm")));
        targetView.addFsToIndexes(normFormAnnotation);

        Type partOfSpeechTagType = typeSystem
            .getType("edu.umn.biomedicus.uima.type1_6.PartOfSpeechTag");

        AnnotationFS partOfSpeechAnnotation = targetView
            .createAnnotation(partOfSpeechTagType, begin, end);
        String tokenPOS = fromAnnotation.getStringValue(fromType.getFeatureByBaseName("tokenPOS"));

        String updatedPos = PartsOfSpeech.forTagWithFallback(tokenPOS.toUpperCase().trim())
            .orElseThrow(() -> new IllegalStateException("Not found: " + tokenPOS))
            .toString();

        partOfSpeechAnnotation
            .setStringValue(partOfSpeechTagType.getFeatureByBaseName("partOfSpeech"),
                updatedPos);
        targetView.addFsToIndexes(partOfSpeechAnnotation);

        return Arrays.asList(parseToken, normFormAnnotation, partOfSpeechAnnotation);
      }
    });

    list.add(new TypeConversion() {
      @Override
      public String sourceTypeName() {
        return "edu.umn.biomedicus.mtsamples.types.Sentence";
      }

      @Override
      public List<FeatureStructure> doMigrate(CAS sourceView, CAS targetView,
          FeatureStructure from,
          Type fromType) {
        AnnotationFS mtSentence = (AnnotationFS) from;

        Type sentenceType = sourceView.getTypeSystem()
            .getType("edu.umn.biomedicus.uima.type1_6.Sentence");

        AnnotationFS sentenceAnnotation = targetView
            .createAnnotation(sentenceType, mtSentence.getBegin(), mtSentence.getEnd());

        targetView.addFsToIndexes(sentenceAnnotation);

        return Collections.singletonList(sentenceAnnotation);
      }
    });

    list.add(new TypeConversion() {
      @Override
      public String sourceTypeName() {
        return "edu.umn.biomedicus.mtsamples.types.Term";
      }

      @Override
      public List<FeatureStructure> doMigrate(CAS sourceView, CAS targetView,
          FeatureStructure from,
          Type fromType) {
        AnnotationFS mtTerm = (AnnotationFS) from;
        FsAccessor mtTermAccess = new FsAccessor(mtTerm);

        TypeSystem typeSystem = sourceView.getTypeSystem();

        Type termType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.DictionaryTerm");
        Type conceptType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.DictionaryConcept");

        int begin = mtTerm.getBegin();
        int end = mtTerm.getEnd();
        AnnotationFS term = targetView.createAnnotation(termType, begin, end);

        AnnotationFS mtConcept = (AnnotationFS) mtTermAccess.getFeatureValue("termConcept");
        FsAccessor mtConceptAccess = new FsAccessor(mtConcept);

        String conceptId = mtConceptAccess.getStringValue("conceptId");
        String conceptTypeValue = mtConceptAccess.getStringValue("conceptType");
        String conceptSource = mtConceptAccess.getStringValue("conceptSource");
        float conceptConfidence = mtConceptAccess.getFloatValue("conceptConfidence");

        List<FeatureStructure> concepts = new ArrayList<>();

        Matcher cuiMatcher = CUI.CUI_PATTERN.matcher(conceptId);
        while (cuiMatcher.find()) {
          int cuiStart = cuiMatcher.start();
          int cuiEnd = cuiMatcher.end();

          AnnotationFS conceptAnnotation = targetView.createAnnotation(conceptType, begin, end);
          FsAccessor conceptAccessor = new FsAccessor(conceptAnnotation);

          String cui = conceptId.substring(cuiStart, cuiEnd);
          conceptAccessor.setFeatureValue("identifier", cui);
          conceptAccessor.setFeatureValue("semanticType", conceptTypeValue);
          conceptAccessor.setFeatureValue("source", conceptSource);
          conceptAccessor.setFeatureValue("confidence", (double) conceptConfidence);

          targetView.addFsToIndexes(conceptAnnotation);
          concepts.add(conceptAnnotation);
        }

        int size = concepts.size();
        ArrayFS fsArray = targetView.createArrayFS(size);
        for (int i = 0; i < size; i++) {
          fsArray.set(i, concepts.get(i));
        }
        term.setFeatureValue(termType.getFeatureByBaseName("concepts"), fsArray);

        targetView.addFsToIndexes(term);

        concepts.add(0, term);

        boolean termNegation = mtTermAccess.getBooleanValue("termNegation");

        if (termNegation) {
          Type negationType = typeSystem
              .getType("edu.umn.biomedicus.uima.type1_6.Negated");

          AnnotationFS negated = targetView.createAnnotation(negationType, begin, end);
          targetView.addFsToIndexes(negated);
          concepts.add(negated);
        }

        String termCertainty = mtTermAccess.getStringValue("termCertainty");
        if (termCertainty != null && (termCertainty.charAt(0) == 'p'
            || termCertainty.charAt(0) == 'P')) {
          Type probableType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.Probable");

          AnnotationFS probable = targetView.createAnnotation(probableType, begin, end);
          targetView.addFsToIndexes(probable);
          concepts.add(probable);
        }

        String termAspect = mtTermAccess.getStringValue("termAspect");
        if (termAspect != null && (termAspect.charAt(0) == 'h' || termAspect.charAt(0) == 'H'
            || termAspect.charAt(0) == 'p' || termAspect.charAt(0) == 'P')) {
          Type historicalType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.Historical");

          AnnotationFS historical = targetView.createAnnotation(historicalType, begin, end);
          targetView.addFsToIndexes(historical);
          concepts.add(historical);
        }

        return concepts;
      }
    });

    return list;
  }

  @Override
  public boolean deleteByDefault() {
    return true;
  }

  @Override
  public List<String> typesNotDefaulted() {
    return Collections.emptyList();
  }

  @Override
  public void setupView(CAS fromView, CAS toView) {
    toView.setDocumentText(fromView.getDocumentText());
  }

  @Override
  public void setupDocument(CAS oldCAS, CAS newCAS) {
    TypeSystem typeSystem = oldCAS.getTypeSystem();
    Type sdiType = typeSystem
        .getType("edu.umn.biomedicus.mtsamples.types.SourceDocumentInformation");

    Type metadataType = typeSystem.getType("edu.umn.biomedicus.type.MetaData");

    Type documentType = typeSystem.getType("edu.umn.biomedicus.mtsamples.types.Document");

    AnnotationIndex<AnnotationFS> annotationIndex = oldCAS.getAnnotationIndex(metadataType);
    FSIterator<AnnotationFS> iterator = annotationIndex.iterator();
    String documentId;
    if (iterator.hasNext()) {
      AnnotationFS next = iterator.next();
      FsAccessor metadataAccessor = new FsAccessor(next);
      documentId = metadataAccessor.getStringValue("documentId");
    } else {
      documentId = UUID.randomUUID().toString();
    }

    assert documentId != null : "should not be null at this point";

    CASDocument casDocument = CASDocument.initialize(null, newCAS, documentId);

    FSIterator<FeatureStructure> allSDI = oldCAS.getIndexRepository().getAllIndexedFS(sdiType);
    if (allSDI.hasNext()) {
      FeatureStructure sdi = allSDI.next();
      FsAccessor fsAccessor = new FsAccessor(sdi);
      casDocument.putMetadata("uri", fsAccessor.getStringValue("uri"));

      casDocument.putMetadata("offsetInSource",
          String.valueOf(fsAccessor.getIntValue("offsetInSource")));

      casDocument.putMetadata("documentSize",
          String.valueOf(fsAccessor.getIntValue("documentSize")));

      casDocument.putMetadata("lastSegment",
          String.valueOf(fsAccessor.getBooleanValue("lastSegment")));
    }

    FSIterator<FeatureStructure> allDocument = oldCAS.getIndexRepository()
        .getAllIndexedFS(documentType);
    if (allDocument.hasNext()) {
      FeatureStructure document = allDocument.next();
      FsAccessor fsAccessor = new FsAccessor(document);

      casDocument.putMetadata("sampleId", fsAccessor.getStringValue("sampleId"));
      casDocument.putMetadata("sampleName", fsAccessor.getStringValue("sampledName"));
      casDocument.putMetadata("typeId", fsAccessor.getStringValue("typeId"));
      casDocument.putMetadata("typeName", fsAccessor.getStringValue("typeName"));
    }
  }

  @Override
  public List<Pair<String, String>> viewMigrations() {
    return Collections.singletonList(Pair.of("SystemView", "SystemView"));
  }
}
