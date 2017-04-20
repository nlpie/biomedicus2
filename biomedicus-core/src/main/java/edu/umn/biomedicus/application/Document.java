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

package edu.umn.biomedicus.application;

import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.Map;
import java.util.Optional;

/**
 * A single processing artifact and all the data that it contains
 *
 * @since 1.6.0
 */
public interface Document {
    /**
     *
     *
     * @return
     */
    String getDocumentId();


    /**
     *
     * @param key
     * @return
     */
    Optional<String> getMetadata(String key) throws BiomedicusException;

    Map<String, String> getAllMetadata();

    /**
     *
     * @param key
     * @param value
     */
    void putMetadata(String key, String value) throws BiomedicusException;

    void putAllMetadata(Map<String, String> metadata)
            throws BiomedicusException;

    /**
     *
     * @param name
     * @param text
     * @return
     */
    TextView createTextView(String name, String text)
            throws BiomedicusException;

    /**
     *
     * @param name
     * @return
     */
    TextView getTextView(String name) throws BiomedicusException;
}
