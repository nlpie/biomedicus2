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

package edu.umn.biomedicus.uima.util;

import com.google.inject.Inject;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates the category on MTSamples documents using the document id.
 */
public class MtsamplesCategoryAnnotator implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MtsamplesCategoryAnnotator.class);
    private final Document document;

    @Inject
    public MtsamplesCategoryAnnotator(Document document) {
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        String documentId = document.getDocumentId();
        int underscore = documentId.indexOf("_");
        String category = documentId.substring(0, underscore);
        document.putMetadata("category", category);
    }
}
