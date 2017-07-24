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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.terms.AbstractTermIndex;
import edu.umn.biomedicus.common.terms.TermIndex;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;

/**
 * An index of String terms to and from integers.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
class MapDbTermIndex extends AbstractTermIndex implements Closeable, TermIndexBuilder {

  private final IndexTreeList<String> instances;

  private final BTreeMap<String, Integer> indexes;

  private boolean open = true;

  MapDbTermIndex(DB db, String dbIdentifier) {
    instances = db.indexTreeList(dbIdentifier + "Instances", Serializer.STRING)
        .createOrOpen();

    indexes = db.treeMap(dbIdentifier + "Indexes", Serializer.STRING_DELTA,
        Serializer.INTEGER).createOrOpen();
  }

  @Override
  public void addTerm(String string) {
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
    }

    if (indexes.containsKey(string)) {
      return;
    }
    int index = instances.size();
    instances.add(string);
    indexes.put(string, index);
  }

  void addTerm(CharSequence term) {
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
    }
    addTerm(term.toString());
  }

  @Override
  public boolean contains(String string) {
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
    }
    return indexes.containsKey(string);
  }

  @Override
  protected String getTerm(int termIdentifier) {
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
    }
    String s = instances.get(termIdentifier);
    if (s == null) {
      throw new IllegalArgumentException("Term not found: " + s);
    }
    return s;
  }

  @Override
  protected int getIdentifier(@Nullable CharSequence term) {
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
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
    if (!open) {
      throw new IllegalStateException("Term index has been closed");
    }
    return instances.size();
  }

  @Override
  public void close() throws IOException {
    open = false;
  }

  TermIndex inMemory(Boolean inMemory) {
    return inMemory ? new HashTermIndex(this) : this;
  }
}
