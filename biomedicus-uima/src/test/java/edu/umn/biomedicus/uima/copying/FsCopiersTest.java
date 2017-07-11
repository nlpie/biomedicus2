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

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.testng.annotations.Test;

/**
 * Test class for {@link FsCopiers}
 *
 * @author Ben Knoll
 */
public class FsCopiersTest {

  @Tested
  FsCopiers fsCopiers;

  @Injectable
  UnaryOperator<FeatureStructure> callback;

  @Injectable
  FeatureCopiers featureCopiers;

  @Injectable
  Type fromType, toType;

  @Injectable
  CAS fromCas, toCas;

  boolean[] boolArr = new boolean[]{true, false};

  @Injectable
  FeatureStructure ref1, ref2, toRef1, toRef2;

  FeatureStructure[] fsArr = new FeatureStructure[]{ref1, ref2};

  @Injectable
  Feature f1, f2, f3;


  @Test
  public void testCopyPrimitiveArray(@Injectable BooleanArrayFS from, @Injectable BooleanArrayFS to)
      throws Exception {
    new Expectations() {{
      from.getType();
      result = fromType;
      fromType.getName();
      result = "uima.cas.BooleanArray";

      from.toArray();
      result = boolArr;
      from.size();
      result = 2;
      to.copyFromArray(boolArr, 0, 0, 2);

      to.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(from, to);

    new Verifications() {{
      toCas.addFsToIndexes(to);
    }};
  }

  @Test
  public void testCopyReferenceArray(@Injectable ArrayFS from, @Injectable ArrayFS to)
      throws Exception {
    new Expectations() {{
      from.getType();
      result = fromType;
      fromType.getName();
      result = "uima.cas.FSArray";

      from.get(0);
      result = ref1;
      from.get(1);
      result = ref2;
      from.size();
      result = 2;

      callback.apply(ref1);
      result = toRef1;
      to.set(0, toRef1);
      callback.apply(ref2);
      result = toRef2;
      to.set(1, toRef2);

      to.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(from, to);

    new Verifications() {{
      toCas.addFsToIndexes(to);
    }};
  }

  @Test
  public void testCopyGenericFs(@Injectable FeatureStructure from, @Injectable FeatureStructure to)
      throws Exception {
    List<Feature> featuresList = new ArrayList<>();
    featuresList.add(f1);
    featuresList.add(f2);
    featuresList.add(f3);

    new Expectations() {{
      from.getType();
      result = fromType;
      fromType.getName();
      result = "someType";

      fromType.getFeatures();
      result = featuresList;

      to.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(from, to);

    new Verifications() {{
      featureCopiers.copyFeature(f1, from, to);
      featureCopiers.copyFeature(f2, from, to);
      featureCopiers.copyFeature(f3, from, to);
      toCas.addFsToIndexes(to);
    }};
  }
}