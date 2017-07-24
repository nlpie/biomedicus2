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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CommonArrayFS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;

/**
 * Class which assists in creating Uima {@code FeatureStructure}s.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class FsConstructors {

  /**
   * The CAS structure that all created {@code FeatureStructure}s will be saved in.
   */
  private final CAS targetCas;

  /**
   * A {@link Map} of String UIMA type names to their constructor functions.
   */
  private final Map<String, UnaryOperator<FeatureStructure>> constructors;

  /**
   * Constructor for {@code FsConstructors} which creates the {@code FeatureStructure}s in the
   * designated {@link CAS} object.
   *
   * @param targetCas {@code CAS} to create new {@code FeatureStructure}s in.
   */
  FsConstructors(CAS targetCas) {
    this.targetCas = targetCas;
    constructors = new HashMap<>();
    constructors.put(CAS.TYPE_NAME_BOOLEAN_ARRAY, arrayFactory(targetCas::createBooleanArrayFS));
    constructors.put(CAS.TYPE_NAME_BYTE_ARRAY, arrayFactory(targetCas::createByteArrayFS));
    constructors.put(CAS.TYPE_NAME_DOUBLE_ARRAY, arrayFactory(targetCas::createDoubleArrayFS));
    constructors.put(CAS.TYPE_NAME_FLOAT_ARRAY, arrayFactory(targetCas::createFloatArrayFS));
    constructors.put(CAS.TYPE_NAME_FS_ARRAY, arrayFactory(targetCas::createArrayFS));
    constructors.put(CAS.TYPE_NAME_LONG_ARRAY, arrayFactory(targetCas::createLongArrayFS));
    constructors.put(CAS.TYPE_NAME_INTEGER_ARRAY, arrayFactory(targetCas::createIntArrayFS));
    constructors.put(CAS.TYPE_NAME_SHORT_ARRAY, arrayFactory(targetCas::createShortArrayFS));
    constructors.put(CAS.TYPE_NAME_STRING_ARRAY, arrayFactory(targetCas::createStringArrayFS));
  }

  /**
   * Returns a function which creates a specific UIMA array type.
   *
   * @param constructor method on the {@code CAS} object which returns the new array.
   * @return newly created array feature structure
   */
  private UnaryOperator<FeatureStructure> arrayFactory(
      Function<Integer, FeatureStructure> constructor) {
    return (FeatureStructure featureStructure) -> constructor
        .apply(((CommonArrayFS) featureStructure).size());
  }

  /**
   * Function for creating generic UIMA {@code FeatureStructure}s.
   *
   * @param featureStructure {@code FeatureStructure} to create a new object of the same type in the
   * target cas.
   * @return newly created {@code FeatureStructure} matching the type of the parameter {@code
   * FeatureStructure}
   */
  private FeatureStructure defaultCreateType(FeatureStructure featureStructure) {
    String typeName = featureStructure.getType().getName();
    Type targetType = targetCas.getTypeSystem().getType(typeName);
    return targetCas.createFS(targetType);
  }

  /**
   * Creates an instance of the same type as the {@code FeatureStructure passed in}.
   *
   * @param featureStructure {@code FeatureStructure} to create a new instance matching
   * @return newly initialized {@code FeatureStructure} with the same type as the parameter's
   */
  public FeatureStructure createNewInstanceOfSameType(FeatureStructure featureStructure) {
    String typeName = featureStructure.getType().getName();
    return constructors.getOrDefault(typeName, this::defaultCreateType).apply(featureStructure);
  }
}
