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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.jetbrains.annotations.NotNull;

/**
 * UIMA implementation of the {@link Document} interface. Uses an empty "metadata" view to hold
 * metadata as well as the document identifier.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class CASDocument implements Document {

  @Nullable
  private final LabelAdapters labelAdapters;

  private final CAS cas;
  private final CAS metadata;

  private final Type metadataType;
  private final Feature keyFeature;
  private final Feature valueFeature;

  private final String documentId;

  private final FSIndex<FeatureStructure> metadataIndex;

  CASDocument(@Nullable LabelAdapters labelAdapters, CAS cas) {
    this.labelAdapters = labelAdapters;
    this.cas = cas;

    TypeSystem typeSystem = cas.getTypeSystem();
    metadataType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
    keyFeature = metadataType.getFeatureByBaseName("key");
    valueFeature = metadataType.getFeatureByBaseName("value");

    metadata = cas.getView("metadata");
    Type idType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature idFeat = idType.getFeatureByBaseName("documentId");
    FSIndexRepository indexRepository = metadata.getIndexRepository();
    documentId = indexRepository.getIndex("documentId", idType).iterator().get()
        .getStringValue(idFeat);
    metadataIndex = indexRepository.getIndex("metadata", metadataType);
  }

  CASDocument(@Nullable LabelAdapters labelAdapters,
      CAS cas,
      String documentId) {
    this.labelAdapters = labelAdapters;
    this.cas = cas;

    TypeSystem typeSystem = cas.getTypeSystem();
    metadataType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
    keyFeature = metadataType.getFeatureByBaseName("key");
    valueFeature = metadataType.getFeatureByBaseName("value");

    metadata = cas.createView("metadata");
    metadata.setDocumentText("");

    Type idType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature idFeat = idType.getFeatureByBaseName("documentId");
    this.documentId = documentId;
    FeatureStructure documentIdFs = metadata.createFS(idType);
    documentIdFs.setStringValue(idFeat, documentId);
    metadata.addFsToIndexes(documentIdFs);
    metadataIndex = metadata.getIndexRepository().getIndex("metadata", metadataType);
  }

  public static CASDocument open(@Nullable LabelAdapters labelAdapters, CAS top) {
    return new CASDocument(labelAdapters, top);
  }

  public static CASDocument initialize(
      @Nullable LabelAdapters labelAdapters,
      CAS top,
      String documentId
  ) {
    return new CASDocument(labelAdapters, top, documentId);
  }

  @Override
  public String getDocumentId() {
    return documentId;
  }

  @Override
  public Map<String, String> getMetadata() {
    return new AbstractMap<String, String>() {
      @Override
      public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
          @Override
          public Iterator<Entry<String, String>> iterator() {
            FSIterator<FeatureStructure> it = metadataIndex.iterator();
            return new Iterator<Entry<String, String>>() {
              @Override
              public boolean hasNext() {
                return it.hasNext();
              }

              @Override
              public Entry<String, String> next() {
                FeatureStructure next = it.next();
                return new AbstractMap.SimpleImmutableEntry<>(next.getStringValue(keyFeature),
                    next.getStringValue(valueFeature));
              }
            };
          }

          @Override
          public int size() {
            return metadataIndex.size();
          }
        };
      }

      @Nullable
      @Override
      public String get(Object key) {
        if (!(key instanceof String)) {
          return null;
        }
        FeatureStructure check = metadata.createFS(metadataType);
        check.setStringValue(keyFeature, (String) key);
        FeatureStructure fs = metadataIndex.find(check);
        return fs.getStringValue(valueFeature);
      }

      @Nullable
      @Override
      public String put(String key, String value) {
        FeatureStructure check = metadata.createFS(metadataType);
        check.setStringValue(keyFeature, key);
        FeatureStructure fs = metadataIndex.find(check);
        String existing = null;
        if (fs != null) {
          existing = fs.getStringValue(valueFeature);
          metadata.removeFsFromIndexes(fs);
        } else {
          fs = check;
        }
        fs.setStringValue(valueFeature, value);
        metadata.addFsToIndexes(fs);
        return existing;
      }

      @Override
      public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
          return false;
        }
        FeatureStructure check = metadata.createFS(metadataType);
        check.setStringValue(keyFeature, (String) key);
        return metadataIndex.contains(check);
      }
    };
  }

  public CAS getCas() {
    return cas;
  }

  @NotNull
  @Override
  public Map<String, LabeledText> getLabeledTexts() {
    return new AbstractMap<String, LabeledText>() {
      @Override
      public Set<Entry<String, LabeledText>> entrySet() {
        return new AbstractSet<Entry<String, LabeledText>>() {
          @Override
          public Iterator<Entry<String, LabeledText>> iterator() {
            Iterator<CAS> viewIterator = cas.getViewIterator();
            return new Iterator<Entry<String, LabeledText>>() {
              @Nullable CAS nextCas;

              {
                advance();
              }

              void advance() {
                if (!viewIterator.hasNext()) {
                  nextCas = null;
                  return;
                }
                CAS next = viewIterator.next();
                if (!next.getViewName().equals("metadata") && !next.getViewName().equals(CAS.NAME_DEFAULT_SOFA)) {
                  nextCas = next;
                } else {
                  advance();
                }
              }

              @Override
              public boolean hasNext() {
                return nextCas != null;
              }

              @Override
              public Entry<String, LabeledText> next() {
                if (nextCas == null) {
                  throw new NoSuchElementException();
                }
                CAS value = nextCas;
                advance();
                return new AbstractMap.SimpleImmutableEntry<>(value.getViewName(),
                    new CASLabeledText(value, labelAdapters));
              }
            };
          }

          @Override
          public int size() {
            int count = 0;
            Iterator<CAS> viewIterator = cas.getViewIterator();
            while (viewIterator.hasNext()) {
              count++;
              viewIterator.next();
            }
            return count;
          }
        };
      }
    };
  }

  @NotNull
  @Override
  public LabeledText attachText(@NotNull String id, @NotNull String text) {
    CAS view = cas.createView(id);
    view.setDocumentText(text);
    return new CASLabeledText(view, labelAdapters);
  }
}
