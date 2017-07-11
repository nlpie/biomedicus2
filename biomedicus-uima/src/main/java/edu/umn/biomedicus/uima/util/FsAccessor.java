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

package edu.umn.biomedicus.uima.util;

import javax.annotation.Nullable;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;

/**
 * Allows you to set and retrieve features dynamically on UIMA FeatureStructures. It's probably
 * slow, but not as slow as the time it takes to program a bunch of
 * {@link Type#getFeatureByBaseName(String)} calls. Only use for utility / non-production code. It's
 * missing some things, add them if you need them.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class FsAccessor {

  private final FeatureStructure featureStructure;
  private final Type type;

  public FsAccessor(FeatureStructure featureStructure) {
    this.featureStructure = featureStructure;
    type = featureStructure.getType();
  }

  /**
   * Sets the feature value based on the type that you're using to set, make sure that your
   * primitives are casted to the right type.
   *
   * @param name the feature name
   * @param value the value to set
   */
  public void setFeatureValue(String name, Object value) {
    Feature feature = getFeature(name);
    if (value instanceof String) {
      featureStructure.setStringValue(feature, (String) value);
    } else if (value instanceof Integer) {
      featureStructure.setIntValue(feature, ((Integer) value));
    } else if (value instanceof Long) {
      featureStructure.setLongValue(feature, ((Long) value));
    } else if (value instanceof Double) {
      featureStructure.setDoubleValue(feature, ((Double) value));
    } else if (value instanceof Float) {
      featureStructure.setFloatValue(feature, ((Float) value));
    } else if (value instanceof FeatureStructure) {
      featureStructure.setFeatureValue(feature, ((FeatureStructure) value));
    } else if (value instanceof Byte) {
      featureStructure.setByteValue(feature, ((Byte) value));
    }
  }

  @Nullable
  public FeatureStructure getFeatureValue(String name) {
    return featureStructure.getFeatureValue(getFeature(name));
  }

  @Nullable
  public String getStringValue(String name) {
    return featureStructure.getStringValue(getFeature(name));
  }

  public double getDoubleValue(String name) {
    return featureStructure.getDoubleValue(getFeature(name));
  }

  public float getFloatValue(String name) {
    return featureStructure.getFloatValue(getFeature(name));
  }

  public int getIntValue(String name) {
    return featureStructure.getIntValue(getFeature(name));
  }

  public long getLongValue(String name) {
    return featureStructure.getLongValue(getFeature(name));
  }

  public Feature getFeature(String name) {
    return type.getFeatureByBaseName(name);
  }

  public boolean getBooleanValue(String name) {
    return featureStructure.getBooleanValue(getFeature(name));
  }
}
