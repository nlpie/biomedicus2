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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test for {@link ViewMigratorJCasMultiplier}
 */
public class ViewMigratorJCasMultiplierTest {

  @Tested
  ViewMigratorJCasMultiplier viewMigratorJCasMultiplier;

  @Mocked
  UimaContext aContext;

  @Mocked
  ViewCopier viewCopier;

  @Mocked
  MockViewMigrator viewMigrator;

  @Injectable JCas aJCas;

  @Injectable JCas newCas;

  @Injectable JCas sourceView;
  @Injectable JCas targetView;
  @Injectable JCas otherView;
  @Injectable JCas otherTargetView;
  @Injectable JCas otherView2;
  @Injectable JCas otherTargetView2;
  @Injectable JCas initialView;
  @Injectable JCas initialViewCopy;

  @Injectable JCas sourceViewCopy;

  @Test
  public void testSetsParameterValues() throws Exception {
    new Expectations() {{
      aContext.getConfigParameterValue("sourceViewName");
      result = "sourceView";
      aContext.getConfigParameterValue("targetViewName");
      result = "targetView";
      aContext.getConfigParameterValue("deleteOriginalView");
      result = false;
      aContext.getConfigParameterValue("viewMigratorClass");
      result = MockViewMigrator.class.getCanonicalName();
    }};

    viewMigratorJCasMultiplier.initialize(aContext);

    AssertJUnit.assertEquals("sourceView",
        Deencapsulation.getField(viewMigratorJCasMultiplier, "sourceViewName"));
    AssertJUnit.assertEquals("targetView",
        Deencapsulation.getField(viewMigratorJCasMultiplier, "targetViewName"));
    assertEquals(false,
        (boolean) Deencapsulation.getField(viewMigratorJCasMultiplier, "deleteOriginalView"));
  }

  @Test
  public void testHasNext() throws Exception {

    new Expectations() {{
      aContext.getConfigParameterValue("sourceViewName"); result = "sourceView";
      aContext.getConfigParameterValue("targetViewName"); result = "targetView";
      aContext.getConfigParameterValue("deleteOriginalView"); result = false;
      aContext.getConfigParameterValue("viewMigratorClass"); result = MockViewMigrator.class.getCanonicalName();
      aContext.getEmptyCas(JCas.class); result = newCas;
    }};
    viewMigratorJCasMultiplier.initialize(aContext);
    viewMigratorJCasMultiplier.process(aJCas);

    assertTrue(viewMigratorJCasMultiplier.hasNext());

    viewMigratorJCasMultiplier.next();

    assertFalse(viewMigratorJCasMultiplier.hasNext());
  }

  @Test
  public void testProcessKeepOriginal() throws Exception {
    new Expectations() {{
      aContext.getConfigParameterValue("sourceViewName");
      result = "sourceView";
      aContext.getConfigParameterValue("targetViewName");
      result = "targetView";
      aContext.getConfigParameterValue("deleteOriginalView");
      result = false;
      aContext.getConfigParameterValue("viewMigratorClass");
      result = MockViewMigrator.class.getCanonicalName();

      aContext.getEmptyCas(JCas.class); result = newCas;
      aJCas.getViewIterator(); result = Collections.singletonList(sourceView).iterator();

      sourceView.getViewName(); result = "sourceView";

      newCas.createView("sourceView"); result = sourceViewCopy;
      newCas.createView("targetView"); result = targetView;

      new ViewCopier();
      result = viewCopier;
    }};
    viewMigratorJCasMultiplier.initialize(aContext);
    viewMigratorJCasMultiplier.process(aJCas);
    assertNotNull(viewMigratorJCasMultiplier.next());

    new Verifications() {{
      viewCopier.migrate(sourceView, sourceViewCopy);
      viewMigrator.migrate(sourceView, targetView);
    }};
  }

  @Test
  public void testProcessDeleteOriginal() throws Exception {
    new Expectations() {{
      aContext.getConfigParameterValue("sourceViewName");
      result = "sourceView";
      aContext.getConfigParameterValue("targetViewName");
      result = "targetView";
      aContext.getConfigParameterValue("deleteOriginalView");
      result = true;
      aContext.getConfigParameterValue("viewMigratorClass");
      result = MockViewMigrator.class.getCanonicalName();
      aContext.getEmptyCas(JCas.class);
      result = newCas;
      aJCas.getViewIterator(); result = Collections.singletonList(sourceView).iterator();

      sourceView.getViewName();
      result = "sourceView";

      newCas.createView("targetView"); result = targetView;
    }};
    viewMigratorJCasMultiplier.initialize(aContext);
    viewMigratorJCasMultiplier.process(aJCas);
    assertNotNull(viewMigratorJCasMultiplier.next());

    new Verifications() {{
      new ViewCopier(); times = 0;
      newCas.createView("sourceView"); times = 0;
      viewMigrator.migrate(sourceView, targetView); times = 1;
      viewCopier.migrate(withAny(aJCas), withAny(aJCas)); times = 0;
    }};
  }

  @Test
  public void testProcessMultiview() throws Exception {
    new Expectations() {{
      aContext.getConfigParameterValue("sourceViewName");
      result = "sourceView";
      aContext.getConfigParameterValue("targetViewName");
      result = "targetView";
      aContext.getConfigParameterValue("deleteOriginalView");
      result = true;
      aContext.getConfigParameterValue("viewMigratorClass");
      result = MockViewMigrator.class.getCanonicalName();
      aContext.getEmptyCas(JCas.class);
      result = newCas;
      aJCas.getViewIterator();
      result = Arrays.asList(sourceView, otherView, otherView2, initialView).iterator();

      sourceView.getViewName();
      result = "sourceView";
      otherView.getViewName();
      result = "other1";
      otherView2.getViewName();
      result = "other2";
      initialView.getViewName();
      result = "_InitialView";

      new ViewCopier();
      result = viewCopier;
      times = 3;

      newCas.createView("targetView");
      result = targetView;
      newCas.createView("other1");
      result = otherTargetView;
      newCas.createView("other2");
      result = otherTargetView2;
      newCas.getView("_InitialView");
      result = initialViewCopy;
    }};

    viewMigratorJCasMultiplier.initialize(aContext);
    viewMigratorJCasMultiplier.process(aJCas);
    assertNotNull(viewMigratorJCasMultiplier.next());

    new Verifications() {{
      newCas.createView("_InitialView");
      times = 0;
      viewMigrator.migrate(sourceView, targetView);
      viewCopier.migrate(otherView, otherTargetView);
      viewCopier.migrate(otherView2, otherTargetView2);
      viewCopier.migrate(initialView, initialViewCopy);
      viewCopier.migrate(sourceView, withAny(aJCas));
      times = 0;
    }};
  }
}