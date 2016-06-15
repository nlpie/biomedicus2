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

package edu.umn.biomedicus.application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@ProcessorScoped
class DocumentProcessorRunner implements CollectionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessorRunner.class);

    private final Injector injector;

    private final String documentProcessorClassName;

    @Inject
    DocumentProcessorRunner(Injector injector,
                            @ProcessorSetting("documentProcessor") String documentProcessorClassName) {
        this.injector = injector;
        this.documentProcessorClassName = documentProcessorClassName;
    }

    @Override
    public void processDocument(Document document) throws BiomedicusException {
        try {
            Map<Key<?>, Object> seededObjects = new HashMap<>();
            seededObjects.put(Key.get(Document.class), document);
            BiomedicusScopes.runInDocumentScope(() -> {
                Class<? extends DocumentProcessor> aClass = Class.forName(documentProcessorClassName)
                        .asSubclass(DocumentProcessor.class);
                injector.getInstance(aClass).process();
                return null;
            }, seededObjects);
        } catch (Exception e) {
            LOGGER.error("Error during processing");
            throw new BiomedicusException(e);
        }
    }

    @Override
    public void allDocumentsProcessed() throws BiomedicusException {

    }
}
