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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.framework.LifecycleManaged;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The default implementation of the Vocabulary class which uses
 * the {@link VocabularyStore} implementation to load the vocabulary.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
class DefaultVocabulary implements LifecycleManaged, Vocabulary {

  private final VocabularyStore store;

  private final BidirectionalDictionary wordsIndex;

  private final BidirectionalDictionary bidirectionalDictionary;

  private final BidirectionalDictionary normsIndex;

  DefaultVocabulary(VocabularyStore store) {
    this.store = store;
    this.wordsIndex = store.getWords();
    this.bidirectionalDictionary = store.getTerms();
    this.normsIndex = store.getNorms();
  }

  @Override
  public BidirectionalDictionary getWordsIndex() {
    return wordsIndex;
  }

  @Override
  public BidirectionalDictionary getTermsIndex() {
    return bidirectionalDictionary;
  }

  @Override
  public BidirectionalDictionary getNormsIndex() {
    return normsIndex;
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    try {
      store.close();
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }

  @Singleton
  public static class Loader extends DataLoader<Vocabulary> {

    private final VocabularyStore store;

    @Inject
    Loader(VocabularyStore store) {
      this.store = store;
    }

    @Override
    protected Vocabulary loadModel() throws BiomedicusException {
      try {
        store.open();
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
      return new DefaultVocabulary(store);
    }
  }
}
