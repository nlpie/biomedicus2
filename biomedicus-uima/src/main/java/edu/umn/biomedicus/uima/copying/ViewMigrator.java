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

package edu.umn.biomedicus.uima.copying;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

/**
 * Interface for a class which migrates the data from one UIMA view to another.
 *
 * @since 1.3.0
 */
public interface ViewMigrator {

  /**
   * Performs the migration from the source view to the target view.
   *
   * @param source source view
   * @param target target view
   */
  void migrate(JCas source, JCas target);

  /**
   * Performs the migration from the source view to the target view.
   *
   * @param source source view
   * @param target target view
   * @throws CASException if there is a problem getting the JCas of the views.
   */
  default void migrate(CAS source, CAS target) throws CASException {
    migrate(source.getJCas(), target.getJCas());
  }
}
