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

package edu.umn.biomedicus.uima.migration;

import edu.umn.biomedicus.common.tuples.Pair;
import java.util.List;
import org.apache.uima.cas.CAS;

/**
 * Performs a type system migration between two different UIMA type systems by converting XMI files.
 *
 * @since 1.7.0
 */
public interface TypeSystemMigration {

  /**
   * Returns the programmatic type conversions to be performed.
   *
   * @return a list of {@link TypeConversion} classes
   */
  List<TypeConversion> getTypeConversions();

  /**
   * Decides whether the migration should copy over, or delete types by default.
   *
   * @return implement this to return false if you want feature structures to be copied by default,
   * or return true if you want types to be deleted by default.
   */
  boolean deleteByDefault();

  /**
   * A list of the types where the default action is not taken. If the default action is to delete,
   * these types will be copied to the new view. If the default action is to copy, these types will
   * be deleted.
   *
   * @return list of qualified UIMA type names where the default action is not taken
   * @see #deleteByDefault()
   */
  List<String> typesNotDefaulted();

  /**
   * Performs any initial work in creating individual views.
   *
   * @param fromView the view being moved from
   * @param toView view being moved to
   */
  void setupView(CAS fromView, CAS toView);

  /**
   * Set up the document at the very top level.
   *
   * @param oldCAS the old CAS from the previous document
   * @param newCAS the new CAS
   */
  void setupDocument(CAS oldCAS, CAS newCAS);

  /**
   * Returns the pairs of source destination view names.
   */
  List<Pair<String, String>> viewMigrations();
}
