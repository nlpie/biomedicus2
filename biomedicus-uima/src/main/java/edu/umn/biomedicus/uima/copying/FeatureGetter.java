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

package edu.umn.biomedicus.uima.copying;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;

/**
 * Interface for the methods on {@link FeatureStructure} which return the value of a feature.
 *
 * @since 1.3.0
 */
@FunctionalInterface
public interface FeatureGetter<T> {

  /**
   * Returns the value of a feature from a {@code FeatureStructure} given the {@code Feature}.
   *
   * @param featureStructure {@code FeatureStructure} to retrieve value from
   * @param feature {@code Feature} to retrieve
   * @return the value of the feature
   */
  T get(FeatureStructure featureStructure, Feature feature);
}
