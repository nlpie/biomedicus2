/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.rtfrewriting;

import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import edu.umn.nlpengine.Artifact;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Inserts placeholders for annotations into RTF documents.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class AnnotationInsertingWriter extends CasAnnotator_ImplBase {

  /**
   * The annotation types to insert into the the rtf document.
   */
  @Nullable
  private String[] annotationTypes = null;

  /**
   * The output directory.
   */
  @Nullable
  private Path outputDir;

  private String documentName;

  private String rtfDocumentName;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    annotationTypes = (String[]) aContext.getConfigParameterValue("annotationTypes");

    outputDir = Paths.get((String) aContext.getConfigParameterValue("outputDirectory"));
    try {
      Files.createDirectories(outputDir);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    documentName = ((String) aContext.getConfigParameterValue("documentName"));

    rtfDocumentName = ((String) aContext.getConfigParameterValue("rtfDocumentName"));
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    CAS originalDocumentView = aCAS.getView(rtfDocumentName);
    SymbolIndexedDocument symbolIndexedDocument =
        SymbolIndexedDocument.fromView(originalDocumentView);

    CAS view = aCAS.getView(documentName);

    TreeSet<Integer> covered = new TreeSet<>();
    for (String annotationType : Objects.requireNonNull(annotationTypes)) {
      Type type = view.getTypeSystem().getType(annotationType);

      AnnotationIndex<Annotation> annotationIndex = view.getAnnotationIndex(type);

      for (Annotation annotation : annotationIndex) {
        IntStream.rangeClosed(annotation.getBegin(), annotation.getEnd()).forEach(covered::add);
      }
    }

    Iterator<Integer> iterator = covered.iterator();
    int next = iterator.next();
    int last = -1;
    while (iterator.hasNext()) {
      int first = next;
      while (iterator.hasNext()) {
        last = next;
        next = iterator.next();
        if (next - last > 1) {
          break;
        }
      }
      RegionTaggerBuilder.create()
          .withBeginTag("\\u2222221B ")
          .withEndTag("\\u2222221E ")
          .withSymbolIndexedDocument(symbolIndexedDocument)
          .withDestinationName(documentName)
          .withBegin(first)
          .withEnd(last)
          .createRegionTagger()
          .tagRegion();
    }

    String rewrittenDocument = symbolIndexedDocument.getDocument();

    Artifact artifact = UimaAdapters.getArtifact(aCAS, null);

    Path file = outputDir.resolve(artifact.getArtifactID() + ".rtf");

    try (BufferedWriter bufferedWriter = Files
        .newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
      bufferedWriter.write(rewrittenDocument);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}
