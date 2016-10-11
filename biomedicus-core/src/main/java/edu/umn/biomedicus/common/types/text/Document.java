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

package edu.umn.biomedicus.common.types.text;

import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.annotation.Nullable;
import java.io.Reader;

/**
 * A biomedicus basic unit for a document of text.
 * <p>This class will be implemented for each backend, so the biomedicus pipeline is designed to be agnostic
 * about how the data is stored. The UIMA example is edu.umn.biomedicus.adapter.JCasDocument</p>
 */
public interface Document {

    /**
     * Returns a reader for the document text
     *
     * @return a java reader for the document text
     */
    Reader getReader();

    /**
     * Gets the entire text of the document
     *
     * @return document text
     */
    String getText();

    @Nullable
    String getDocumentId();

    void setDocumentId(String documentId);

    /**
     *
     * @param key
     * @return
     */
    @Nullable
    String getMetadata(String key) throws BiomedicusException;

    /**
     *
     * @param key
     * @param value
     */
    void setMetadata(String key, String value) throws BiomedicusException;

    Document getSiblingDocument(String identifier) throws BiomedicusException;

    <T> LabelIndex<T> getLabelIndex(Class<T> labelClass);

    <T> Labeler<T> getLabeler(Class<T> labelClass);
}
