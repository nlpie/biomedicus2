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

package edu.umn.biomedicus.uima.copying;

import org.apache.uima.UIMAFramework;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLInputSource;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Integration test for {@link FeatureCopiers}.
 */
public class FeatureCopiersTestIT {

    private JCas oldView;

    private JCas newView;

    @BeforeTest
    public void setUp() throws Exception {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("edu/umn/biomedicus/types/TypeSystem.xml");
        XMLInputSource tsInput = new XMLInputSource(resourceAsStream, Paths.get("").toFile());
        TypeSystemDescription typeSystemDescription = UIMAFramework.getXMLParser().parseTypeSystemDescription(tsInput);
        JCas cas = CasCreationUtils.createCas(typeSystemDescription, null, null).getJCas();
        oldView = cas.createView("old");
        newView = cas.createView("new");
    }

    @Test
    public void testCopyAnnotationBeginFeature() throws Exception {
        oldView.setDocumentText("blah");
        newView.setDocumentText("blah");

        Annotation oldAnnotation = new Annotation(oldView);
        oldAnnotation.setBegin(0);
        oldAnnotation.setEnd(4);
        oldAnnotation.addToIndexes();

        Annotation newAnnotation = new Annotation(newView);

        FeatureCopiers featureCopiers = new FeatureCopiers((fs) -> { return new Annotation(newView);});
        featureCopiers.copyFeature(oldAnnotation.getType().getFeatureByBaseName("begin"), oldAnnotation, newAnnotation);

        Assert.assertEquals(newAnnotation.getBegin(), oldAnnotation.getBegin());
    }
}