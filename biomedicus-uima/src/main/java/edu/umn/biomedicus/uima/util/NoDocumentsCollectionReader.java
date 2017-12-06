/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

import java.io.IOException;
import java.util.NoSuchElementException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.util.Progress;

/**
 * A collection reader which does nothing
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class NoDocumentsCollectionReader extends CollectionReader_ImplBase {

  @Override
  public void getNext(CAS cas) throws IOException, CollectionException {
    throw new NoSuchElementException();
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return false;
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[0];
  }

  @Override
  public void close() throws IOException {

  }
}
