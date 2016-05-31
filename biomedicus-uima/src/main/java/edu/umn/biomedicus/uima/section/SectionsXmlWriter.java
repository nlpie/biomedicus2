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

package edu.umn.biomedicus.uima.section;

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.type.SectionAnnotation;
import edu.umn.biomedicus.type.SubSectionAnnotation;
import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Writes sections as XML elements to a file.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SectionsXmlWriter extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SectionsXmlWriter.class);

    /**
     * DOM document builder.
     */
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    /**
     * XML transformer.
     */
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /**
     * Output directory to write to.
     */
    @Nullable
    private Path outputDir;

    /**
     * Initializes the writer factory
     *
     * @param aContext the uima context, used to get configuration parameters.
     * @throws ResourceInitializationException if our writer factory fails to set the output directory
     */
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        outputDir = Paths.get((String) aContext.getConfigParameterValue("outputDirectory"));

        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AnalysisEngineProcessException(e);
        }

        JCas systemView;
        try {
            systemView = aJCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        String documentText = systemView.getDocumentText();

        Document document = documentBuilder.newDocument();

        Element note = document.createElement("note");

        document.appendChild(note);

        int last = 0;
        AnnotationIndex<Annotation> subsectionIndex = systemView.getAnnotationIndex(SubSectionAnnotation.type);
        AnnotationIndex<Annotation> sectionIndex = systemView.getAnnotationIndex(SectionAnnotation.type);
        for (Annotation annotation : sectionIndex) {
            SectionAnnotation sectionAnnotation = (SectionAnnotation) annotation;
            int begin = sectionAnnotation.getBegin();
            if (begin < last) {
                LOGGER.warn("Malformed document, section beginning before the end of a previous section.");
                malformedDocument(systemView , "section_overlap");
                return;
            }
            String beforeSection = documentText.substring(last, begin);
            Text beforeSectionText = document.createTextNode(beforeSection);
            note.appendChild(beforeSectionText);
            int sectionEnd = sectionAnnotation.getEnd();

            Element section = document.createElement("section");
            section.setAttribute("kind", sectionAnnotation.getKind());
            section.setAttribute("title", sectionAnnotation.getSectionTitle());

            int consumedTextIndex = begin;
            FSIterator<Annotation> subiterator = subsectionIndex.subiterator(sectionAnnotation);
            while (subiterator.hasNext()) {
                SubSectionAnnotation subSectionAnnotation = (SubSectionAnnotation) subiterator.next();
                int subSectionBegin = subSectionAnnotation.getBegin();
                if (subSectionBegin < consumedTextIndex) {
                    LOGGER.warn("Malformed document, subsection that overlaps other subsection or section begin.");
                    malformedDocument(systemView, "subsection_overlap");
                    return;
                }
                String beforeSubSection = documentText.substring(consumedTextIndex, subSectionBegin);
                Text beforeSubSectionText = document.createTextNode(beforeSubSection);
                section.appendChild(beforeSubSectionText);
                Element subSection = document.createElement("subsection");
                subSection.setAttribute("kind", subSectionAnnotation.getKind());
                subSection.setAttribute("title", subSectionAnnotation.getTitle());
                int subSectionEnd = subSectionAnnotation.getEnd();
                subSection.setTextContent(documentText.substring(subSectionBegin, subSectionEnd));
                section.appendChild(subSection);
                consumedTextIndex = subSectionEnd;
            }
            if (sectionEnd < consumedTextIndex) {
                LOGGER.warn("Malformed document, subsection that spans over the end of a section.");
                malformedDocument(systemView, "subsection_overlap");
                return;
            }

            String sectionText = documentText.substring(consumedTextIndex, sectionEnd);
            Text sectionTextNode = document.createTextNode(sectionText);
            section.appendChild(sectionTextNode);

            note.appendChild(section);

            last = sectionEnd;
        }

        String afterSections = documentText.substring(last);
        Text afterSectionsTextNode = document.createTextNode(afterSections);
        note.appendChild(afterSectionsTextNode);
        DOMSource source = new DOMSource(document);
        String other = FileNameProviders.fromSystemView(systemView, ".xml");
        Path outFilePath = outputDir.resolve(other);
        StreamResult result = new StreamResult(outFilePath.toFile());


        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new AnalysisEngineProcessException(e);
        }
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void malformedDocument(JCas systemView, String reason) throws AnalysisEngineProcessException {
        Path path = outputDir.resolve("malformedDocuments.txt");

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            ClinicalNoteAnnotation note = (ClinicalNoteAnnotation) systemView.getAnnotationIndex(ClinicalNoteAnnotation.type).iterator().next();
            String documentId = note.getDocumentId();
            LOGGER.warn("Failed document: {}", documentId);
            bufferedWriter.write(documentId + "\t" + reason + "\n");
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
