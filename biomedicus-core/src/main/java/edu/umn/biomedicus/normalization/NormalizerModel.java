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

package edu.umn.biomedicus.normalization;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.framework.LifecycleManaged;
import javax.annotation.Nullable;

/**
 * Interface for a normalizer model.
 */
@ProvidedBy(NormalizerModelLoader.class)
public interface NormalizerModel extends LifecycleManaged {

  /**
   * Gets the term index identifier and its string form
   *
   * @param termPos the term and part of speech
   * @return the term and string
   */
  @Nullable
  TermString get(TermPos termPos);
}
