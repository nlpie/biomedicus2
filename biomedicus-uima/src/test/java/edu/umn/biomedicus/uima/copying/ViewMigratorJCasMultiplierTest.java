/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

import mockit.*;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.AssertJUnit.*;

/**
 * Test for {@link ViewMigratorJCasMultiplier}
 */
public class ViewMigratorJCasMultiplierTest {
    @Tested ViewMigratorJCasMultiplier viewMigratorJCasMultiplier;

    @Mocked UimaContext aContext;

    @Mocked Iterator<JCas> viewIterator;

    @Mocked ViewCopier viewCopier;

    @Mocked MockViewMigrator viewMigrator;

    @Test
    public void testSetsParameterValues() throws Exception {
        new Expectations() {{
            aContext.getConfigParameterValue("sourceViewName"); result = "sourceView";
            aContext.getConfigParameterValue("targetViewName"); result = "targetView";
            aContext.getConfigParameterValue("deleteOriginalView"); result = false;
            aContext.getConfigParameterValue("viewMigratorClass"); result = MockViewMigrator.class.getCanonicalName();
        }};

        viewMigratorJCasMultiplier.initialize(aContext);

        AssertJUnit.assertEquals("sourceView", Deencapsulation.getField(viewMigratorJCasMultiplier, "sourceViewName"));
        AssertJUnit.assertEquals("targetView", Deencapsulation.getField(viewMigratorJCasMultiplier, "targetViewName"));
        assertEquals(false, (boolean) Deencapsulation.getField(viewMigratorJCasMultiplier, "deleteOriginalView"));
    }

    @Test
    public void testHasNext(@Injectable JCas aJCas, @Injectable JCas newCas) throws Exception {

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
    public void testProcessKeepOriginal(@Injectable JCas aJCas,
                                        @Injectable JCas newCas,
                                        @Injectable JCas sourceView,
                                        @Injectable JCas sourceViewCopy,
                                        @Injectable JCas targetView) throws Exception {
        new Expectations() {{
            aContext.getConfigParameterValue("sourceViewName"); result = "sourceView";
            aContext.getConfigParameterValue("targetViewName"); result = "targetView";
            aContext.getConfigParameterValue("deleteOriginalView"); result = false;
            aContext.getConfigParameterValue("viewMigratorClass"); result = MockViewMigrator.class.getCanonicalName();

            aContext.getEmptyCas(JCas.class); result = newCas;
            aJCas.getViewIterator(); result = viewIterator;
            viewIterator.hasNext(); result = new boolean[]{true, false}; times = 2;
            viewIterator.next(); result = sourceView;

            sourceView.getViewName(); result = "sourceView";

            onInstance(newCas).createView("sourceView"); result = sourceViewCopy;
            onInstance(newCas).createView("targetView"); result = targetView;

            new ViewCopier(); result = viewCopier;
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
    public void testProcessDeleteOriginal(@Injectable JCas aJCas,
                                          @Injectable JCas newCas,
                                          @Injectable JCas sourceView,
                                          @Injectable JCas targetView) throws Exception {
        new Expectations() {{
            aContext.getConfigParameterValue("sourceViewName"); result = "sourceView";
            aContext.getConfigParameterValue("targetViewName"); result = "targetView";
            aContext.getConfigParameterValue("deleteOriginalView"); result = true;
            aContext.getConfigParameterValue("viewMigratorClass"); result = MockViewMigrator.class.getCanonicalName();
            aContext.getEmptyCas(JCas.class); result = newCas;
            aJCas.getViewIterator(); result = viewIterator;
            viewIterator.hasNext(); result = new boolean[]{true, false}; times = 2;
            viewIterator.next(); result = sourceView;

            sourceView.getViewName(); result = "sourceView";

            onInstance(newCas).createView("targetView"); result = targetView;
        }};
        viewMigratorJCasMultiplier.initialize(aContext);
        viewMigratorJCasMultiplier.process(aJCas);
        assertNotNull(viewMigratorJCasMultiplier.next());

        new Verifications() {{
            new ViewCopier(); times = 0;
            onInstance(newCas).createView("sourceView"); times = 0;
            viewMigrator.migrate(sourceView, targetView); times = 1;
            viewCopier.migrate(withAny(aJCas), withAny(aJCas)); times = 0;
        }};
    }

    @Test
    public void testProcessMultiview(@Injectable JCas aJCas,
                                     @Injectable JCas newCas,
                                     @Injectable JCas sourceView,
                                     @Injectable JCas targetView,
                                     @Injectable JCas otherView,
                                     @Injectable JCas otherTargetView,
                                     @Injectable JCas otherView2,
                                     @Injectable JCas otherTargetView2,
                                     @Injectable JCas initialView,
                                     @Injectable JCas initialViewCopy) throws Exception {
        new Expectations() {{
            aContext.getConfigParameterValue("sourceViewName"); result = "sourceView";
            aContext.getConfigParameterValue("targetViewName"); result = "targetView";
            aContext.getConfigParameterValue("deleteOriginalView"); result = true;
            aContext.getConfigParameterValue("viewMigratorClass"); result = MockViewMigrator.class.getCanonicalName();
            aContext.getEmptyCas(JCas.class); result = newCas;
            onInstance(aJCas).getViewIterator(); result = viewIterator;
            viewIterator.hasNext(); result = new boolean[]{true, true, true, true, false};
            viewIterator.next(); returns(sourceView, otherView, otherView2, initialView);

            onInstance(sourceView).getViewName(); result = "sourceView";
            onInstance(otherView).getViewName(); result = "other1";
            onInstance(otherView2).getViewName(); result = "other2";
            onInstance(initialView).getViewName(); result = "_InitialView";

            new ViewCopier(); result = viewCopier; times = 3;

            onInstance(newCas).createView("targetView"); result = targetView;
            onInstance(newCas).createView("other1"); result = otherTargetView;
            onInstance(newCas).createView("other2"); result = otherTargetView2;
            onInstance(newCas).getView("_InitialView"); result = initialViewCopy;
        }};

        viewMigratorJCasMultiplier.initialize(aContext);
        viewMigratorJCasMultiplier.process(aJCas);
        assertNotNull(viewMigratorJCasMultiplier.next());

        new Verifications() {{
            onInstance(newCas).createView("_InitialView"); times = 0;
            viewMigrator.migrate(sourceView, targetView);
            viewCopier.migrate(otherView, otherTargetView);
            viewCopier.migrate(otherView2, otherTargetView2);
            viewCopier.migrate(initialView, initialViewCopy);
            viewCopier.migrate(sourceView, withAny(aJCas)); times = 0;
        }};
    }
}