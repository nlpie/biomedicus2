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

package edu.umn.biomedicus.uima.files;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import mockit.*;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Unit Test for {@link BaseFileNameProvider}.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileNameProviderTest {

    @Mocked JFSIndexRepository jfsIndexRepository;

    @Mocked FSIterator<TOP> fsIterator;

    @Mocked ClinicalNoteAnnotation clinicalNoteAnnotation;

    @Mocked UUID uuid;

    @Test
    public void testFromSystemView(@Injectable JCas systemView) throws Exception {
        new Expectations() {{
            systemView.getJFSIndexRepository(); result = jfsIndexRepository;
            jfsIndexRepository.getAllIndexedFS(ClinicalNoteAnnotation.type); result = fsIterator;
            fsIterator.hasNext(); result = true;
            fsIterator.next(); result = clinicalNoteAnnotation;
            clinicalNoteAnnotation.getDocumentId(); result = "docId";
        }};

        FileNameProvider fileNameProvider = FileNameProviders.fromSystemView(systemView, ".txt");
        Assert.assertNotNull(fileNameProvider);

        @SuppressWarnings("unchecked")
        String fileNameField = Deencapsulation.getField(fileNameProvider, "fileName");
        Assert.assertEquals("docId.txt", fileNameField);
    }

    @Test
    public void testFromSystemViewWithNullIndexRepository(@Injectable JCas systemView) throws Exception {
        new Expectations() {{
            systemView.getJFSIndexRepository(); result = null;
            UUID.randomUUID(); result = uuid;
            uuid.toString(); result = "UUID";
        }};

        FileNameProvider fileNameProvider = FileNameProviders.fromSystemView(systemView, ".txt");
        Assert.assertNotNull(fileNameProvider);

        @SuppressWarnings("unchecked")
        String fileNameField = Deencapsulation.getField(fileNameProvider, "fileName");
        Assert.assertEquals("unidentified-UUID.txt", fileNameField);
    }

    @Test
    public void testFromSystemViewWithNotHasNext(@Injectable JCas systemView) throws Exception {
        new Expectations() {{
            systemView.getJFSIndexRepository(); result = jfsIndexRepository;
            jfsIndexRepository.getAllIndexedFS(ClinicalNoteAnnotation.type); result = fsIterator;
            fsIterator.hasNext(); result = false;
            UUID.randomUUID(); result = uuid;
            uuid.toString(); result = "UUID";
        }};

        FileNameProvider fileNameProvider = FileNameProviders.fromSystemView(systemView, ".txt");
        Assert.assertNotNull(fileNameProvider);

        @SuppressWarnings("unchecked")
        String fileNameField = Deencapsulation.getField(fileNameProvider, "fileName");
        Assert.assertEquals("unidentified-UUID.txt", fileNameField);

        new Verifications() {{
            fsIterator.next(); times = 0;
        }};
    }

    @Test
    public void testFromSystemViewWithNullClinicalNoteAnnotation(@Injectable JCas systemView) throws Exception {
        new Expectations() {{
            systemView.getJFSIndexRepository(); result = jfsIndexRepository;
            jfsIndexRepository.getAllIndexedFS(ClinicalNoteAnnotation.type); result = fsIterator;
            fsIterator.hasNext(); result = true;
            fsIterator.next(); result = null;
            UUID.randomUUID(); result = uuid;
            uuid.toString(); result = "UUID";
        }};

        FileNameProvider fileNameProvider = FileNameProviders.fromSystemView(systemView, ".txt");
        Assert.assertNotNull(fileNameProvider);

        @SuppressWarnings("unchecked")
        String fileNameField = Deencapsulation.getField(fileNameProvider, "fileName");
        Assert.assertEquals("unidentified-UUID.txt", fileNameField);
    }

    @Test
    public void testFromInitialView(@Injectable JCas initialView,
                                    @Injectable JCas systemView) throws Exception {
        new Expectations() {{
            onInstance(initialView).getView("SystemView"); result = systemView;
        }};

        FileNameProviders.fromInitialView(initialView, ".txt");

        new Verifications() {{
            FileNameProviders.fromSystemView(onInstance(systemView), ".txt");
        }};
    }

    @Test(expectedExceptions = BiomedicusException.class)
    public void testFromInitialViewCASException(@Injectable JCas initialView,
                                                @Injectable JCas systemView) throws Exception {
        new Expectations() {{
            onInstance(initialView).getView("SystemView"); result = new CASException();
        }};

        FileNameProviders.fromInitialView(initialView, ".txt");
    }

    @Test
    public void testFromDocument(@Injectable Document document) throws Exception {
        new Expectations() {{
            document.getIdentifier(); result = "docId";
        }};

        FileNameProvider fileNameProvider = FileNameProviders.fromDocument(document, ".txt");

        @SuppressWarnings("unchecked")
        String fileNameField = Deencapsulation.getField(fileNameProvider, "fileName");
        Assert.assertEquals("docId.txt", fileNameField);
    }

    @Test
    public void testAppendFileName(@Injectable Path outPath) throws Exception {
        FileNameProvider fileNameProvider = Deencapsulation.newInstance(BaseFileNameProvider.class, "fileName.txt");

        String path = fileNameProvider.getFileName();
        Assert.assertEquals(path, "fileName.txt");
    }
}