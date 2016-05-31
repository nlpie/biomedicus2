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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class RtfTestCollectionReader extends CollectionReader_ImplBase {
    private boolean returned = false;

    @Override
    public void getNext(CAS cas) throws IOException, CollectionException {
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource("edu/umn/biomedicus/uima/rtf/data/97_100.rtf");

        assert resource != null;
        String rtfDoc;
        try {
            rtfDoc = new String(Files.readAllBytes(Paths.get(resource.toURI())));
        } catch (URISyntaxException e) {
            throw new CollectionException(e);
        }

        CAS view = cas.createView(Views.ORIGINAL_DOCUMENT_VIEW);
        view.setDocumentText(rtfDoc);

        JCas jCas;
        try {
            jCas = view.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        ClinicalNoteAnnotation documentAnnotation = new ClinicalNoteAnnotation(jCas, 0, rtfDoc.length());
        documentAnnotation.setDocumentId("97_100");
        documentAnnotation.addToIndexes();
        returned = true;
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return !returned;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] {new ProgressImpl(returned ? 1 : 0, 1, "documents")};
    }

    @Override
    public void close() throws IOException {

    }
}
