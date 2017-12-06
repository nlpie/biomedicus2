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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.rtf.reader.State;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;

/**
 * A mapping from some kind of rtf property state to an annotation type for emission in UIMA.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlJavaTypeAdapter(PropertyCasMappingAdapter.class)
public class PropertyCasMapping {

  private final String propertyGroup;

  private final String propertyName;

  private final String annotationClassName;

  private final int minimumValue;

  @Nullable
  private final Integer maximumValue;

  private final boolean valueIncluded;

  private final boolean zeroLengthEmitted;

  public PropertyCasMapping(String propertyGroup,
      String propertyName,
      String annotationClassName,
      int minimumValue,
      @Nullable Integer maximumValue,
      boolean valueIncluded,
      boolean zeroLengthEmitted) {
    this.propertyGroup = propertyGroup;
    this.propertyName = propertyName;
    this.annotationClassName = annotationClassName;
    this.minimumValue = minimumValue;
    this.maximumValue = maximumValue;
    this.valueIncluded = valueIncluded;
    this.zeroLengthEmitted = zeroLengthEmitted;
  }

  @Nullable
  AnnotationFS getAnnotation(CAS cas, int begin, int end, int value) {
    if (begin < 0) {
      throw new IllegalArgumentException("Begin: " + begin + "before 0.");
    }

    if (end < begin) {
      throw new IllegalArgumentException(
          annotationClassName + " - illegal annotation span at begin: " + begin
              + " end: " + end);
    }

    if (!zeroLengthEmitted && end == begin) {
      return null;
    }

    TypeSystem typeSystem = cas.getTypeSystem();
    Type type = typeSystem.getType(annotationClassName);
    AnnotationFS annotation = cas.createAnnotation(type, begin, end);
    if (valueIncluded) {
      Feature valueFeature = type.getFeatureByBaseName("value");
      annotation.setIntValue(valueFeature, value);
    }
    return annotation;
  }

  boolean test(int value) {
    return (value >= minimumValue && (maximumValue == null || value <= maximumValue));
  }

  int getPropertyValue(State state) {
    return state.getPropertyValue(propertyGroup, propertyName);
  }

  public String getPropertyGroup() {
    return propertyGroup;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getAnnotationClassName() {
    return annotationClassName;
  }

  public int getMinimumValue() {
    return minimumValue;
  }

  @Nullable
  public Integer getMaximumValue() {
    return maximumValue;
  }

  public boolean isValueIncluded() {
    return valueIncluded;
  }

  public boolean isZeroLengthEmitted() {
    return zeroLengthEmitted;
  }
}
