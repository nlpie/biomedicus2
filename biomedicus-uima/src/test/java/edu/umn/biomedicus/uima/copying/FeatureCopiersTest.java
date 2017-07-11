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
import org.testng.annotations.Test;

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
  public void testPrimitiveCopier() throws Exception {
    new Expectations() {{
      onInstance(fromFeature).getRange();
      result = fromType;
      onInstance(fromType).getName();
      result = CAS.TYPE_NAME_BOOLEAN;
      onInstance(fromFs).getBooleanValue(fromFeature);
      result = true;
      onInstance(toFs).getType();
      result = toType;
      onInstance(fromFeature).getShortName();
      result = "featName";
      onInstance(toType).getFeatureByBaseName("featName");
      result = toFeature;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new FullVerificationsInOrder() {{
      onInstance(toFs).setBooleanValue(toFeature, true);
    }};
  }

  @Test
  public void testReferenceCopier() throws Exception {
    new Expectations() {{
      onInstance(fromFeature).getRange();
      result = fromType;
      onInstance(fromType).getName();
      result = "SomeOtherTypeName";
      onInstance(fromFs).getFeatureValue(fromFeature);
      result = fromReference;
      callback.apply(onInstance(fromReference));
      result = toReference;
      typeSystem.getType(CAS.TYPE_NAME_STRING);
      result = stringType;
      typeSystem.subsumes(onInstance(stringType), onInstance(fromType));
      result = false;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new Verifications() {{
      toFs.setFeatureValue(toFeature, toReference);
    }};
  }

  @Test
  public void testEnumeratedStringCopier() throws Exception {
    new Expectations() {{
      onInstance(fromFeature).getRange();
      result = fromType;
      onInstance(fromType).getName();
      result = "SomeOtherTypeName";
      onInstance(fromFs).getStringValue(fromFeature);
      result = "aString";
      typeSystem.getType(CAS.TYPE_NAME_STRING);
      result = stringType;
      typeSystem.subsumes(onInstance(stringType), onInstance(fromType));
      result = true;
    }};

    featureCopiers.copyFeature(fromFeature, fromFs, toFs);

    new Verifications() {{
      onInstance(toFs).setStringValue(onInstance(toFeature), "aString");
      callback.apply(withInstanceOf(FeatureStructure.class));
      times = 0;
    }};
  }
}