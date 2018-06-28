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
import org.junit.jupiter.api.Test;

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

  @Injectable BooleanArrayFS boolFrom, boolTo;

  @Injectable FeatureStructure fsFrom, fsTo;

  @Injectable ArrayFS arrayFrom, arrayTo;


  @Test
  public void testCopyPrimitiveArray() {
    new Expectations() {{
      boolFrom.getType();
      result = fromType;
      fromType.getName();
      result = "uima.cas.BooleanArray";

      boolFrom.toArray();
      result = boolArr;
      boolFrom.size();
      result = 2;
      boolTo.copyFromArray(boolArr, 0, 0, 2);

      boolTo.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(boolFrom, boolTo);

    new Verifications() {{
      toCas.addFsToIndexes(boolTo);
    }};
  }

  @Test
  public void testCopyReferenceArray() {
    new Expectations() {{
      arrayFrom.getType();
      result = fromType;
      fromType.getName();
      result = "uima.cas.FSArray";

      arrayFrom.get(0);
      result = ref1;
      arrayFrom.get(1);
      result = ref2;
      arrayFrom.size();
      result = 2;

      callback.apply(ref1);
      result = toRef1;
      arrayTo.set(0, toRef1);
      callback.apply(ref2);
      result = toRef2;
      arrayTo.set(1, toRef2);

      arrayTo.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(arrayFrom, arrayTo);

    new Verifications() {{
      toCas.addFsToIndexes(arrayTo);
    }};
  }

  @Test
  public void testCopyGenericFs() {
    List<Feature> featuresList = new ArrayList<>();
    featuresList.add(f1);
    featuresList.add(f2);
    featuresList.add(f3);

    new Expectations() {{
      arrayFrom.getType();
      result = fromType;
      fromType.getName();
      result = "someType";

      fromType.getFeatures();
      result = featuresList;

      arrayTo.getCAS();
      result = toCas;
    }};

    fsCopiers.copy(arrayFrom, arrayTo);

    new Verifications() {{
      featureCopiers.copyFeature(f1, arrayFrom, arrayTo);
      featureCopiers.copyFeature(f2, arrayFrom, arrayTo);
      featureCopiers.copyFeature(f3, arrayFrom, arrayTo);
      toCas.addFsToIndexes(arrayTo);
    }};
  }
}