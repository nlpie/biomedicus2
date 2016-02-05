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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import mockit.*;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for {@link NewInformationWriterAE}.
 */
public class NewInformationWriterAETest {
    @Tested NewInformationWriterAE newInformationWriterAE;

    @Mocked UimaContext uimaContext;

    @Mocked Paths paths;

    @Mocked Path path;

    @Mocked NewInformationWriterFactory newInformationWriterFactory;

    @Test
    public void testInitialize() throws Exception {
        new Expectations() {{
            uimaContext.getConfigParameterValue("outputDir"); result = "theOutputDir";
            Paths.get("theOutputDir"); result = path;
            NewInformationWriterFactory.createWithOutputDirectory(path); result = newInformationWriterFactory;
        }};

        newInformationWriterAE.initialize(uimaContext);

        @SuppressWarnings("unchecked")
        NewInformationWriterFactory writerFactoryField = Deencapsulation.getField(newInformationWriterAE, "newInformationWriterFactory");
        Assert.assertEquals(newInformationWriterFactory, writerFactoryField);
    }

    @Test(expectedExceptions = ResourceInitializationException.class)
    public void testInitializeThrows() throws Exception {
        new Expectations() {{
            uimaContext.getConfigParameterValue("outputDir"); result = "theOutputDir";
            Paths.get("theOutputDir"); result = path;
            NewInformationWriterFactory.createWithOutputDirectory(path); result = new BiomedicusException();
        }};

        newInformationWriterAE.initialize(uimaContext);
    }

    @Test
    public void testProcess(@Mocked UimaAdapters uimaAdapters,
                            @Mocked JCas initialView,
                            @Mocked Document document) throws Exception {
        Deencapsulation.setField(newInformationWriterAE, "newInformationWriterFactory", newInformationWriterFactory);

        new Expectations() {{
            UimaAdapters.documentFromInitialView(initialView); result = document;
        }};

        newInformationWriterAE.process(initialView);

        new Verifications() {{
            newInformationWriterFactory.writeForDocument(document);
        }};
    }

    @Test(expectedExceptions = AnalysisEngineProcessException.class)
    public void testProcessException(@Mocked UimaAdapters uimaAdapters,
                                     @Mocked JCas initialView,
                                     @Mocked Document document) throws Exception {
        Deencapsulation.setField(newInformationWriterAE, "newInformationWriterFactory", newInformationWriterFactory);

        new Expectations() {{
            UimaAdapters.documentFromInitialView(initialView); result = document;
            newInformationWriterFactory.writeForDocument(document); result = new BiomedicusException();
        }};

        newInformationWriterAE.process(initialView);
    }
}