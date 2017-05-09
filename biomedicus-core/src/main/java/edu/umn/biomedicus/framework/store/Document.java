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

package edu.umn.biomedicus.framework.store;

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
    Optional<String> getMetadata(String key);

    Map<String, String> getAllMetadata();

    /**
     *  @param key
     * @param value
     */
    void putMetadata(String key, String value);

    void putAllMetadata(Map<String, String> metadata);

    /**
     *
     * @param name
     * @return
     */
    Optional<TextView> getTextView(String name);


    TextView.Builder newTextView();
}
