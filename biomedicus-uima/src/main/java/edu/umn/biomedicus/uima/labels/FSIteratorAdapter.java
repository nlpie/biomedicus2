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

package edu.umn.biomedicus.uima.labels;

import java.util.Iterator;
import java.util.function.Function;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

/**
 * Adapts an UIMA {@link org.apache.uima.cas.FSIterator} to a Java {@link java.util.Iterator} of an
 * adapter type using a mapper.
 *
 * @param <T> the adapter to map
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class FSIteratorAdapter<T> implements Iterator<T> {

  /**
   * The FSIterator to adapt.
   */
  private final FSIterator<AnnotationFS> annotationFSIterator;

  /**
   * The function which maps the UIMA Annotation to the BioMedICUS type.
   */
  private final Function<AnnotationFS, T> mapper;

  /**
   * Default constructor.
   *
   * @param annotationFSIterator the FSIterator received from UIMA.
   * @param mapper a function which maps a UIMA annotation to an adapter class.
   */
  public FSIteratorAdapter(FSIterator<AnnotationFS> annotationFSIterator,
      Function<AnnotationFS, T> mapper) {
    this.annotationFSIterator = annotationFSIterator;
    this.mapper = mapper;
  }

  /**
   * Convenience constructor which takes an annotation index and retrieves a disambiguated iterator.
   *
   * @param annotationIndex the annotation index
   * @param mapper a function which maps a UIMA annotation to an adapter class.
   */
  public FSIteratorAdapter(AnnotationIndex<AnnotationFS> annotationIndex,
      Function<AnnotationFS, T> mapper) {
    this(annotationIndex.iterator(), mapper);
  }

  /**
   * Creates an {@link java.util.Iterator} which is bound by an UIMA {@link
   * org.apache.uima.jcas.tcas.Annotation}. Uses the {@link AnnotationIndex#subiterator(AnnotationFS)}
   * functionality within UIMA.
   *
   * @param index the index of annotations to filter
   * @param bound the annotation to use as filter bounds
   * @param mapper the function to map annotations to biomedicus models
   * @param <T> the biomedicus model we are creating an iterator of
   * @return an iterator of biomedicus model classes within the UIMA annotation
   */
  public static <T> Iterator<T> coveredIteratorAdapter(AnnotationIndex<AnnotationFS> index,
      AnnotationFS bound,
      Function<AnnotationFS, T> mapper) {
    FSIterator<AnnotationFS> subiterator = index.subiterator(bound);
    return new FSIteratorAdapter<>(subiterator, mapper);
  }

  @Override
  public boolean hasNext() {
    return annotationFSIterator.hasNext();
  }

  @Override
  public T next() {
    AnnotationFS next = annotationFSIterator.next();
    return mapper.apply(next);
  }
}
