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

import static java.util.Objects.requireNonNull;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter for {@link PropertyCasMapping}.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class PropertyCasMappingAdapter extends
    XmlAdapter<AdaptedPropertyCasMapping, PropertyCasMapping> {

  @Override
  public PropertyCasMapping unmarshal(AdaptedPropertyCasMapping v) throws Exception {
    return new PropertyCasMapping(requireNonNull(v.getPropertyGroup()),
        requireNonNull(v.getPropertyName()),
        requireNonNull(v.getAnnotationClassName()), v.getMinimumValue(), v.getMaximumValue(),
        v.isValueIncluded(), v.isZeroLengthEmitted());
  }

  @Override
  public AdaptedPropertyCasMapping marshal(PropertyCasMapping v) throws Exception {
    AdaptedPropertyCasMapping adaptedPropertyCasMapping = new AdaptedPropertyCasMapping();
    adaptedPropertyCasMapping.setMaximumValue(v.getMaximumValue());
    adaptedPropertyCasMapping.setMinimumValue(v.getMinimumValue());
    adaptedPropertyCasMapping.setAnnotationClassName(v.getAnnotationClassName());
    adaptedPropertyCasMapping.setPropertyGroup(v.getPropertyGroup());
    adaptedPropertyCasMapping.setPropertyName(v.getPropertyName());
    adaptedPropertyCasMapping.setValueIncluded(v.isValueIncluded());
    adaptedPropertyCasMapping.setZeroLengthEmitted(v.isZeroLengthEmitted());
    return adaptedPropertyCasMapping;
  }
}
