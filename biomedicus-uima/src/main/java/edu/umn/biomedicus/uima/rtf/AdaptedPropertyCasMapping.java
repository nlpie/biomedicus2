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

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by benknoll on 7/13/15.
 */
public class AdaptedPropertyCasMapping {

  @Nullable
  private String annotationClassName = null;

  private int minimumValue = 1;

  @Nullable
  private Integer maximumValue = null;

  @Nullable
  private String propertyGroup = null;

  @Nullable
  private String propertyName = null;

  private boolean valueIncluded = true;

  private boolean zeroLengthEmitted = false;

  @Nullable
  @XmlElement(required = true)
  public String getAnnotationClassName() {
    return annotationClassName;
  }

  public void setAnnotationClassName(@Nullable String annotationClassName) {
    this.annotationClassName = annotationClassName;
  }

  @XmlElement(required = true)
  public int getMinimumValue() {
    return minimumValue;
  }

  public void setMinimumValue(int minimumValue) {
    this.minimumValue = minimumValue;
  }

  @Nullable
  @XmlElement
  public Integer getMaximumValue() {
    return maximumValue;
  }

  public void setMaximumValue(@Nullable Integer maximumValue) {
    this.maximumValue = maximumValue;
  }

  @Nullable
  @XmlElement(required = true)
  public String getPropertyGroup() {
    return propertyGroup;
  }

  public void setPropertyGroup(@Nullable String propertyGroup) {
    this.propertyGroup = propertyGroup;
  }

  @Nullable
  @XmlElement(required = true)
  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(@Nullable String propertyName) {
    this.propertyName = propertyName;
  }

  @XmlElement
  public boolean isValueIncluded() {
    return valueIncluded;
  }

  public void setValueIncluded(boolean valueIncluded) {
    this.valueIncluded = valueIncluded;
  }

  @XmlElement
  public boolean isZeroLengthEmitted() {
    return zeroLengthEmitted;
  }

  public void setZeroLengthEmitted(boolean zeroLengthEmitted) {
    this.zeroLengthEmitted = zeroLengthEmitted;
  }
}
