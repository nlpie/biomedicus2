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

package edu.umn.biomedicus.uima.rtf;

import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Responsible for dividing annotations using 0-length marker annotations within those annotations.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class TableAnnotationDivider {

  /**
   * CAS view.
   */
  private final CAS cas;

  /**
   * Annotations to divide.
   */
  @Nullable
  private AnnotationIndex<Annotation> annotations;

  /**
   * Annotations to use as divisions.
   */
  @Nullable
  private AnnotationIndex<Annotation> dividers;

  /**
   * Divided annotations type to create.
   */
  @Nullable
  private Type typeToCreate;

  /**
   * Initializes using a view.
   *
   * @param cas the view.
   */
  private TableAnnotationDivider(CAS cas) {
    this.cas = cas;
  }

  static TableAnnotationDivider in(CAS cas) {
    return new TableAnnotationDivider(cas);
  }

  /**
   * Sets the type to divide.
   *
   * @param annotationType type to divide.
   * @return this object
   */
  TableAnnotationDivider divide(Type annotationType) {
    annotations = cas.getAnnotationIndex(annotationType);
    return this;
  }

  /**
   * Sets the type to use for division.
   *
   * @param dividerType type code for the dividing annotation type.
   * @return this object
   */
  TableAnnotationDivider using(Type dividerType) {
    dividers = cas.getAnnotationIndex(dividerType);
    return this;
  }

  /**
   * Sets the type to create.
   *
   * @param type type to create;
   * @return this object
   */
  TableAnnotationDivider into(Type type) {
    typeToCreate = type;
    return this;
  }

  /**
   * Runs the divider.
   */
  void execute() {
    Objects.requireNonNull(annotations);

    annotations.forEach(this::divideAnnotation);

    annotations = null;
    dividers = null;
  }

  private void divideAnnotation(Annotation annotation) {
    Objects.requireNonNull(typeToCreate);
    Objects.requireNonNull(dividers);

    FSIterator<Annotation> subiterator = dividers.subiterator(annotation);
    int begin = annotation.getBegin();
    while (subiterator.hasNext()) {
      int end = subiterator.next().getBegin();
      cas.addFsToIndexes(cas.createAnnotation(typeToCreate, begin, end));
      begin = end;
    }
  }


}
