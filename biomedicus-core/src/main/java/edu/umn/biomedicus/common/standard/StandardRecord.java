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

package edu.umn.biomedicus.common.standard;

import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 */
public class StandardRecord implements Record {

    private final Map<String, String> metadata = new HashMap<>();

    private final Map<String, StandardDocument> docs = new HashMap<>();

    private final String uniqueIdentifier;

    public StandardRecord(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public String setMetadata(String name, String value) {
        return null;
    }

    @Override
    public String getMetadata(String name) {
        return metadata.get(name);
    }

    @Override
    public Document getDocument(String name) {
        StandardDocument doc = docs.get(name);
        if (doc == null) {
            throw new NoSuchElementException("Document not found: " + name);
        }
        return doc;
    }

    @Override
    public Document createDocument(String name, String documentText) {
        StandardDocument doc = new StandardDocument(documentText);
        docs.put(name, doc);
        return doc;
    }
}
