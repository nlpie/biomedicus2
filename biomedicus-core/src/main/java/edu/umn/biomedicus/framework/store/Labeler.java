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

package edu.umn.biomedicus.framework.store;

/**
 * Adds labels of the parameterized type to the parent text view.
 *
 * @param <T> the type to label
 * @since 1.5.0
 */
public interface Labeler<T> {

  /**
   * Starts a label with the specified value before being prompted for a location to label.
   *
   * @param value the value to label
   * @return a value labeler object which allows this value to be applied to multiple locations.
   */
  ValueLabeler value(T value);

  /**
   * Adds the label directly to the view.
   *
   * @param label the label to add
   */
  void label(Label<T> label);

  /**
   * Adds all of the labels from an iterable to the view
   *
   * @param labels labels to add.
   */
  default void labelAll(Iterable<Label<T>> labels) {
    for (Label<T> label : labels) {
      label(label);
    }
  }
}
