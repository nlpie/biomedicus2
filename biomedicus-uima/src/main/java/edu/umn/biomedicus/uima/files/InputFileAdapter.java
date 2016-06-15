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

package edu.umn.biomedicus.uima.files;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface used to adapt a file to a CAS document. Implementations must have a public 0-arg constructor.
 *
 * @since 1.3.0
 */
public interface InputFileAdapter {
    /**
     * Initializes the adapter using the uima context.
     *
     * @param uimaContext                uima context provided to the collection reader
     * @param processingResourceMetaData the uima processing resource metadata
     */
    default void initialize(UimaContext uimaContext, ProcessingResourceMetaData processingResourceMetaData) {

    }

    /**
     * Adapts the file read by the input stream to a cas document.
     *
     * @param cas         cas for file
     * @param path        the path to the file
     */
    void adaptFile(CAS cas, Path path) throws CollectionException, IOException;

    /**
     * Called when a new type system is passed to the collection reader.
     *
     * @param typeSystem new type system
     */
    default void initTypeSystem(TypeSystem typeSystem) {

    }

    /**
     * Sets the view to load data into. This is an optional method. Some adapters by design may not have a target view,
     * for example, deserializers that handle the entire CAS object.
     *
     * @param viewName name of the target view.
     */
    void setTargetView(String viewName);
}
