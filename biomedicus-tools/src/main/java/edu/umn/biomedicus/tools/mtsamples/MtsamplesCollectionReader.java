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

import com.google.inject.Inject;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import edu.umn.biomedicus.uima.files.InputFileAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.biomedicus.uima.type1_6.Section;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An input file adapter for MTSamples files.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class MtsamplesCollectionReader implements InputFileAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MtsamplesCollectionReader.class);
    private final LabelAdapters labelAdapters;
    @Nullable private String targetViewName;

    @Inject
    public MtsamplesCollectionReader(LabelAdapters labelAdapters) {
        this.labelAdapters = labelAdapters;
    }

    @Override
    public void adaptFile(CAS cas, Path path) throws CollectionException {
        CAS systemView = cas.createView(targetViewName);
        JCas systemJCas;
        try {
            systemJCas = systemView.getJCas();
        } catch (CASException e) {
            logger.error("Problem creating system jCas");
            throw new CollectionException(e);
        }

        StringBuilder documentStringBuilder = new StringBuilder();

        logger.info("Parsing document: {}", path);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("Problem creating document builder");
            throw new CollectionException(e);
        }

        Document doc;
        try (InputStream inputStream = Files.newInputStream(path)) {
            doc = documentBuilder.parse(inputStream);
        } catch (SAXException | IOException e) {
            logger.error("Problem parsing document");
            throw new CollectionException(e);
        }

        Element documentElement = doc.getDocumentElement();
        documentElement.normalize();

        String docTypeId = documentElement.getAttribute("TYPE_ID");
        String docType = documentElement.getAttribute("TYPE");
        String sampleId = documentElement.getAttribute("SAMPLE_ID");
        String sampleName = documentElement.getAttribute("SAMPLE_NAME");

        logger.info("Processing document: {}_{}", docType, sampleId);

        NodeList notesBodyList = doc.getElementsByTagName("NOTES_BODY");

        assert notesBodyList.getLength() == 1;

        Node notesBody = notesBodyList.item(0);

        NodeList childNodes = notesBody.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                logger.debug("Writing non-section text");
                documentStringBuilder.append(childNode.getTextContent());
            } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element sectionElement = (Element) childNode;
                if (sectionElement.getTagName().equals("SECTION")) {
                    int begin = documentStringBuilder.length();

                    String sectionTitle = sectionElement.getAttribute("SEC_TITLE");
                    logger.debug("Writing section: {}", sectionTitle);

                    String sectionText = sectionElement.getTextContent();

                    documentStringBuilder.append(sectionTitle);
                    documentStringBuilder.append('\n');
                    documentStringBuilder.append(sectionText);
                    documentStringBuilder.append("\n\n");

                    int end = documentStringBuilder.length();

                    Section section = new Section(systemJCas, begin, end);
                    section.setKind(sectionTitle);
                    section.addToIndexes();
                } else {
                    logger.warn("Encountered an element other than section in notes body.");
                }
            } else {
                logger.warn("Encountered node other than text or element in notes body.");
            }
        }

        String text = documentStringBuilder.toString();
        systemView.setDocumentText(text);

        try {
            edu.umn.biomedicus.common.types.text.Document document = UimaAdapters.documentFromView(systemView, labelAdapters);
            document.setDocumentId(path.getFileName().toString());
            document.setMetadata("docTypeId", docTypeId);
            document.setMetadata("docType", docType);
            document.setMetadata("sampleId", sampleId);
            document.setMetadata("sampleName", sampleName);
            document.setMetadata("category", sampleName);
        } catch (BiomedicusException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public void setTargetView(String viewName) {
        targetViewName = viewName;
    }
}
