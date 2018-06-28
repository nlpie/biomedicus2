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

import java.util.function.UnaryOperator;
import mockit.Expectations;
import mockit.FullVerificationsInOrder;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link FeatureCopiers}.
 *
 * @author Ben Knoll
 */
public class FeatureCopiersTest {

  @Tested
  FeatureCopiers featureCopiers;

  @Injectable
  UnaryOperator<FeatureStructure> callback;

  @Injectable
  Type fromType, toType, stringType;

  @Mocked
  CAS cas;

  @Mocked
  TypeSystem typeSystem;

  @Injectable
  Feature fromFeature, toFeature;

  @Injectable
  FeatureStructure fromFs, toFs, fromReference, toReference;

  @Test
  public void testPrimitiveCopier() {
    new Expectations() {{
      fromFeature.getRange(); result = fromType;
      fromType.getName(); result = CAS.TYPE_NAME_BOOLEAN;
      fromFs.getBooleanValue(fromFeature); result = true;
      toFs.getType(); result = toType;
      fromFeature.getShortName(); result = "featName";
      toType.getFeatureByBaseName("featName"); result = toFeature;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new FullVerificationsInOrder() {{
      toFs.setBooleanValue(toFeature, true);
    }};
  }

  @Test
  public void testReferenceCopier() {
    new Expectations() {{
      fromFeature.getRange();
      result = fromType;
      fromType.getName();
      result = "SomeOtherTypeName";
      fromFs.getFeatureValue(fromFeature);
      result = fromReference;
      callback.apply(fromReference);
      result = toReference;
      typeSystem.getType(CAS.TYPE_NAME_STRING);
      result = stringType;
      typeSystem.subsumes(stringType, fromType);
      result = false;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new Verifications() {{
      toFs.setFeatureValue(toFeature, toReference);
    }};
  }

  @Test
  public void testEnumeratedStringCopier() {
    new Expectations() {{
      fromFeature.getRange();
      result = fromType;
      fromType.getName();
      result = "SomeOtherTypeName";
      fromFs.getStringValue(fromFeature);
      result = "aString";
      typeSystem.getType(CAS.TYPE_NAME_STRING);
      result = stringType;
      typeSystem.subsumes(stringType, fromType);
      result = true;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new Verifications() {{
      toFs.setStringValue(toFeature, "aString");
      callback.apply(withInstanceOf(FeatureStructure.class));
      times = 0;
    }};
  }
}