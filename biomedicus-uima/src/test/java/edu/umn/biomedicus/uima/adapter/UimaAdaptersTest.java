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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.text.Document;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

/**
 *
 */
public class UimaAdaptersTest {

    @Mocked
    JCasDocument jCasDocument;

    @Test
    public void testDocumentFromView(@Mocked JCas jCas) throws Exception {
        new StrictExpectations() {{
            jCas.getView("blah");
            result = jCas;
            new JCasDocument(jCas);
            result = jCasDocument;
        }};

        Document jCasDocument = UimaAdapters.documentFromView(jCas, "blah");
        assertNotNull(jCasDocument);
    }

    @Test(expectedExceptions = CASException.class)
    public void testDocumentFromViewException(@Mocked JCas jCas) throws Exception {
        new StrictExpectations() {{
            jCas.getView("blah");
            result = new CASException();
        }};

        UimaAdapters.documentFromView(jCas, "blah");
        fail();
    }

    @Test
    public void testDocumentFromInitialView(@Mocked JCas jCas) throws Exception {
        new StrictExpectations() {{
            Deencapsulation.invoke(UimaAdapters.class, "documentFromView", jCas, "SystemView");
        }};

        Document document = UimaAdapters.documentFromInitialView(jCas);
        assertNotNull(document);
    }

    @Test
    public void testGoldDocumentFromInitialView(@Mocked JCas jCas) throws Exception {
        new StrictExpectations() {{
            Deencapsulation.invoke(UimaAdapters.class, "documentFromView", jCas, "GoldView");
        }};

        Document document = UimaAdapters.documentFromInitialView(jCas);
        assertNotNull(document);
    }
}
