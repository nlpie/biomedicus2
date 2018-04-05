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

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapterFactory;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.biomedicus.uima.labels.UimaLabelIndex;
import edu.umn.biomedicus.uima.labels.UimaLabeler;
import edu.umn.nlpengine.AbstractArtifact;
import edu.umn.nlpengine.AbstractDocument;
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;
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
public final class CASArtifact extends AbstractArtifact {

  @Nullable
  private final LabelAdapters labelAdapters;

  private final CAS cas;
  private final CAS metadataCas;

  private final Type metadataType;
  private final Feature keyFeature;
  private final Feature valueFeature;

  private final String documentId;

  private final FSIndex<FeatureStructure> metadataIndex;

  private final CASMetadata casMetadata;

  CASArtifact(
      @Nullable LabelAdapters labelAdapters, CAS cas
  ) {
    this.labelAdapters = labelAdapters;
    this.cas = cas;

    TypeSystem typeSystem = cas.getTypeSystem();
    metadataType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
    keyFeature = metadataType.getFeatureByBaseName("key");
    valueFeature = metadataType.getFeatureByBaseName("value");

    metadataCas = cas.getView("metadata");
    Type idType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature idFeat = idType.getFeatureByBaseName("documentId");
    FSIndexRepository indexRepository = metadataCas.getIndexRepository();
    documentId = indexRepository.getIndex("documentId", idType).iterator().get()
        .getStringValue(idFeat);
    metadataIndex = indexRepository.getIndex("metadata", metadataType);

    casMetadata = new CASMetadata();
  }

  CASArtifact(
      @Nullable LabelAdapters labelAdapters,
      CAS cas,
      String documentId
  ) {
    this.labelAdapters = labelAdapters;
    this.cas = cas;

    TypeSystem typeSystem = cas.getTypeSystem();
    metadataType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
    keyFeature = metadataType.getFeatureByBaseName("key");
    valueFeature = metadataType.getFeatureByBaseName("value");

    metadataCas = cas.createView("metadata");
    metadataCas.setDocumentText("");

    Type idType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature idFeat = idType.getFeatureByBaseName("documentId");
    this.documentId = documentId;
    FeatureStructure documentIdFs = metadataCas.createFS(idType);
    documentIdFs.setStringValue(idFeat, documentId);
    metadataCas.addFsToIndexes(documentIdFs);
    metadataIndex = metadataCas.getIndexRepository().getIndex("metadata", metadataType);

    casMetadata = new CASMetadata();
  }

  CASArtifact(
      @Nullable LabelAdapters labelAdapters,
      Artifact artifact,
      CAS cas
  ) {
    this.labelAdapters = labelAdapters;
    this.cas = cas;

    TypeSystem typeSystem = cas.getTypeSystem();
    metadataType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
    keyFeature = metadataType.getFeatureByBaseName("key");
    valueFeature = metadataType.getFeatureByBaseName("value");

    metadataCas = cas.createView("metadata");
    metadataCas.setDocumentText("");

    Type idType = typeSystem
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature idFeat = idType.getFeatureByBaseName("documentId");
    this.documentId = artifact.getArtifactID();
    FeatureStructure documentIdFs = metadataCas.createFS(idType);
    documentIdFs.setStringValue(idFeat, documentId);
    metadataCas.addFsToIndexes(documentIdFs);
    metadataIndex = metadataCas.getIndexRepository().getIndex("metadata", metadataType);

    casMetadata = new CASMetadata();
    casMetadata.putAll(artifact.getMetadata());

    copyDocuments(artifact);
  }

  public static CASArtifact open(@Nullable LabelAdapters labelAdapters, CAS top) {
    return new CASArtifact(labelAdapters, top);
  }

  public static CASArtifact initialize(
      @Nullable LabelAdapters labelAdapters,
      CAS top,
      String documentId
  ) {
    return new CASArtifact(labelAdapters, top, documentId);
  }

  @NotNull
  @Override
  public String getArtifactID() {
    return documentId;
  }

  @Override
  public Map<String, String> getMetadata() {
    return casMetadata;
  }

  public CAS getCas() {
    return cas;
  }

  @NotNull
  @Override
  public Map<String, Document> getDocuments() {
    return new AbstractMap<String, Document>() {
      @Override
      public Set<Entry<String, Document>> entrySet() {
        return new AbstractSet<Entry<String, Document>>() {
          @Override
          public Iterator<Entry<String, Document>> iterator() {
            Iterator<CAS> viewIterator = cas.getViewIterator();
            return new Iterator<Entry<String, Document>>() {
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
              public Entry<String, Document> next() {
                if (nextCas == null) {
                  throw new NoSuchElementException();
                }
                CAS value = nextCas;
                advance();
                return new AbstractMap.SimpleImmutableEntry<>(value.getViewName(),
                    new CASDocument(value, labelAdapters));
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
  public Document addDocument(@NotNull String name, @NotNull String text) {
    CAS view = cas.createView(name);
    view.setDocumentText(text);
    return new CASDocument(view, labelAdapters);
  }

  private class CASMetadata extends AbstractMap<String, String> {
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
      FeatureStructure check = metadataCas.createFS(metadataType);
      check.setStringValue(keyFeature, (String) key);
      FeatureStructure fs = metadataIndex.find(check);
      return fs.getStringValue(valueFeature);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
      FeatureStructure check = metadataCas.createFS(metadataType);
      check.setStringValue(keyFeature, key);
      FeatureStructure fs = metadataIndex.find(check);
      String existing = null;
      if (fs != null) {
        existing = fs.getStringValue(valueFeature);
        metadataCas.removeFsFromIndexes(fs);
      } else {
        fs = check;
      }
      fs.setStringValue(valueFeature, value);
      metadataCas.addFsToIndexes(fs);
      return existing;
    }

    @Override
    public boolean containsKey(Object key) {
      if (!(key instanceof String)) {
        return false;
      }
      FeatureStructure check = metadataCas.createFS(metadataType);
      check.setStringValue(keyFeature, (String) key);
      return metadataIndex.contains(check);
    }
  }

  private class CASDocument extends AbstractDocument {
    private final CAS view;

    @Nullable
    private final LabelAdapters labelAdapters;

    private final Map<Class<?>, LabelIndex<?>> labelIndices = new HashMap<>();

    CASDocument(CAS view, @Nullable LabelAdapters labelAdapters) {
      super(view.getViewName(), view.getDocumentText());
      this.view = view;
      this.labelAdapters = labelAdapters;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T extends Label> LabelIndex<T> labelIndex(@Nonnull Class<T> labelClass) {
      Preconditions.checkNotNull(labelAdapters);
      LabelIndex<T> labelIndex = (LabelIndex<T>) labelIndices.get(labelClass);
      if (labelIndex == null) {
        LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass)
            .create(view, this);
        labelIndices.put(labelClass, labelIndex = new UimaLabelIndex<>(view, labelAdapter));
      }
      return labelIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T extends Label> Labeler<T> labeler(@Nonnull Class<T> labelClass) {
      Preconditions.checkNotNull(labelAdapters);

      LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass).create(view,
          this);
      return new UimaLabeler<>(labelAdapter, this);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      CASDocument that = (CASDocument) o;

      return view.equals(that.view);
    }

    @Override
    public int hashCode() {
      return view.hashCode();
    }

    @NotNull
    @Override
    public String getArtifactID() {
      return documentId;
    }

    @NotNull
    @Override
    public Map<String, String> getMetadata() {
      return casMetadata;
    }

    @NotNull
    @Override
    public Collection<LabelIndex<?>> labelIndexes() {
      return new AbstractCollection<LabelIndex<?>>() {
        @Override
        public Iterator<LabelIndex<?>> iterator() {
          Iterator<FSIndex<FeatureStructure>> indexes = view.getIndexRepository().getIndexes();

          return new Iterator<LabelIndex<?>>() {
            boolean hasNext;
            LabelIndex<?> next;

            {
              tryAdvance();
            }

            void tryAdvance() {
              FSIndex<FeatureStructure> index = indexes.next();
              Type type = index.getType();
              LabelAdapterFactory<?> factory = labelAdapters.getLabelAdapterFactory(type);
              if (factory != null) {
                hasNext = true;
                next = new UimaLabelIndex<>(cas, factory.create(cas, CASDocument.this));
              } else {
                hasNext = false;
              }
            }

            @Override
            public boolean hasNext() {
              return hasNext;
            }

            @Override
            public LabelIndex<?> next() {
              if (!hasNext) {
                throw new NoSuchElementException("No next label index.");
              }
              LabelIndex<?> index = next;
              tryAdvance();
              return index;
            }
          };
        }

        @Override
        public int size() {
          int size = 0;
          for (LabelIndex<?> ignored : this) {
            size++;
          }
          return size;
        }
      };
    }
  }
}
