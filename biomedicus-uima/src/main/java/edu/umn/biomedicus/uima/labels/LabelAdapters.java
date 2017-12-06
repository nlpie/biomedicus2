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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.framework.LabelAliases;
import edu.umn.nlpengine.TextRange;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class LabelAdapters {

  private final Map<Class<?>, LabelAdapterFactory> factoryMap = new HashMap<>();
  private final LabelAliases labelAliases;

  @Inject
  public LabelAdapters(LabelAliases labelAliases) {
    this.labelAliases = labelAliases;
  }

  public void addLabelAdapter(Class tClass, LabelAdapterFactory labelAdapterFactory) {
    factoryMap.put(tClass, labelAdapterFactory);
    labelAliases.addAlias(tClass.getSimpleName(), tClass);
  }

  public <T extends TextRange> LabelAdapterFactory getLabelAdapterFactory(Class<T> tClass) {
    @SuppressWarnings("unchecked")
    LabelAdapterFactory labelAdapterFactory = (LabelAdapterFactory) factoryMap.get(tClass);
    if (labelAdapterFactory == null) {
      throw new IllegalArgumentException(
          "No label adapter found for class: " + tClass.getCanonicalName());
    }

    return labelAdapterFactory;
  }
}
