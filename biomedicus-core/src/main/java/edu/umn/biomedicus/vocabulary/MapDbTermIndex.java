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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.application.LifecycleManaged;
import edu.umn.biomedicus.common.terms.AbstractTermIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.mapdb.*;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * An index of String terms to and from integers.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
abstract class MapDbTermIndex extends AbstractTermIndex implements LifecycleManaged {
    private final Path dbPath;
    private final String dbIdentifier;
    @Nullable private DB db;
    @Nullable private IndexTreeList<String> instances = null;
    @Nullable private BTreeMap<String, Integer> indexes = null;

    MapDbTermIndex(Path dbPath, String dbIdentifier) {
        this.dbPath = dbPath;
        this.dbIdentifier = dbIdentifier;
    }

    void openForWriting(DB db) {
        this.db = db;
        instances = db.indexTreeList(dbIdentifier + "Instances", Serializer.STRING).create();
        indexes = db.treeMap(dbIdentifier + "Indexes", Serializer.STRING_DELTA, Serializer.INTEGER).create();
    }

    void addTerm(String string) {
        if (db == null || instances == null || indexes == null) {
            throw new IllegalStateException("Index for " + dbIdentifier + " is not open.");
        }
        if (indexes.containsKey(string)) {
            return;
        }

        int index = instances.size();
        instances.add(string);
        indexes.put(string, index);
    }

    void addTerm(CharSequence term) {
        addTerm(term.toString());
    }

    @Override
    public boolean contains(String string) {
        if (db == null || instances == null || indexes == null) {
            throw new IllegalStateException("Index for " + dbIdentifier + " is not open.");
        }
        return indexes.containsKey(string);
    }

    @Override
    protected String getTerm(int termIdentifier) {
        if (db == null || instances == null || indexes == null) {
            throw new IllegalStateException("Index for " + dbIdentifier + " is not open.");
        }
        String s = instances.get(termIdentifier);
        if (s == null) {
            throw new IllegalArgumentException("Term not found: " + s);
        }
        return s;
    }

    @Override
    protected int getIdentifier(@Nullable CharSequence term) {
        if (db == null || instances == null || indexes == null) {
            throw new IllegalStateException("Index for " + dbIdentifier + " is not open.");
        }
        if (term == null) {
            return -1;
        }
        String item = term.toString();
        Integer index = indexes.get(item);
        return index == null ? -1 : index;
    }

    @Override
    public int size() {
        if (db == null || instances == null || indexes == null) {
            throw new IllegalStateException("Index for " + dbIdentifier + " is not open.");
        }
        return instances.size();
    }

    @Override
    public void doStartup() throws BiomedicusException {
        db = DBMaker.fileDB(dbPath.toFile()).fileMmapEnableIfSupported().readOnly().closeOnJvmShutdown().make();
        instances = db.indexTreeList(dbIdentifier + "Instances", Serializer.STRING).open();
        indexes = db.treeMap(dbIdentifier + "Indexes", Serializer.STRING_DELTA, Serializer.INTEGER).open();
    }

    @Override
    public void doShutdown() throws BiomedicusException {
        instances = null;
        indexes = null;
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
