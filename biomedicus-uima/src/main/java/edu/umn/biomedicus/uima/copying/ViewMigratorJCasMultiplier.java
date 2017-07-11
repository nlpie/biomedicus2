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

import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * UIMA Analysis Engine which duplicates a UIMA view, optionally deleting the original view.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class ViewMigratorJCasMultiplier extends JCasMultiplier_ImplBase {

  /**
   * UIMA parameter name for the source view name
   */
  public static final String PARAM_SOURCE_VIEW_NAME = "sourceViewName";

  /**
   * UIMA parameter name for the target view name
   */
  public static final String PARAM_TARGET_VIEW_NAME = "targetViewName";

  /**
   * UIMA parameter for whether we should delete the original view
   */
  public static final String PARAM_DELETE_ORIGINAL_VIEW = "deleteOriginalView";

  /**
   * Fully qualified canonical class name for the {@link ViewMigrator} to use.
   */
  public static final String PARAM_VIEW_MIGRATOR_CLASS = "viewMigratorClass";

  /**
   * The name of the view to copy
   */
  @Nullable
  private String sourceViewName;

  /**
   * The name to create for a new view
   */
  @Nullable
  private String targetViewName;

  /**
   * Whether the original view should be deleted
   */
  @Nullable
  private Boolean deleteOriginalView;

  /**
   * Whether the newly created view with the copy has been returned
   */
  private boolean returned;

  /**
   * The newly created view with the specified copy
   */
  @Nullable
  private JCas newJCas;

  /**
   * View migrator class.
   */
  @Nullable
  private Class<? extends ViewMigrator> viewMigratorClass;

  /**
   * {@inheritDoc} <p>Initializes the 3 parameters, "sourceViewName", "targetViewName", and
   * "deleteOriginalView".</p>
   */
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    sourceViewName = (String) aContext.getConfigParameterValue(PARAM_SOURCE_VIEW_NAME);
    targetViewName = (String) aContext.getConfigParameterValue(PARAM_TARGET_VIEW_NAME);
    deleteOriginalView = (Boolean) aContext.getConfigParameterValue(PARAM_DELETE_ORIGINAL_VIEW);

    try {
      String className = (String) aContext.getConfigParameterValue(PARAM_VIEW_MIGRATOR_CLASS);
      viewMigratorClass = Class.forName(className).asSubclass(ViewMigrator.class);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * {@inheritDoc} <p>Performs the copying of all views in the old view to a new view, and also for
   * the specified view creates a new view optionally copying the old view twice, once to the new
   * name, once to the existing name</p>
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    Objects.requireNonNull(sourceViewName);
    Objects.requireNonNull(targetViewName);
    Objects.requireNonNull(viewMigratorClass);
    returned = false;

    newJCas = getEmptyJCas();

    Objects.requireNonNull(newJCas);

    Iterator<JCas> viewIterator;
    try {
      viewIterator = aJCas.getViewIterator();
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }

    while (viewIterator.hasNext()) {
      JCas view = viewIterator.next();

      String viewName = view.getViewName();
      if (sourceViewName.equals(viewName)) {
        JCas targetView;
        try {
          targetView = newJCas.createView(targetViewName);
        } catch (CASException e) {
          throw new AnalysisEngineProcessException(e);
        }

        try {
          viewMigratorClass.newInstance().migrate(view, targetView);
        } catch (InstantiationException | IllegalAccessException e) {
          throw new AnalysisEngineProcessException(e);
        }
        if (deleteOriginalView != null && deleteOriginalView) {
          continue;
        }
      }
      JCas newView;
      try {
        newView = CAS.NAME_DEFAULT_SOFA.equals(viewName) ? newJCas.getView(viewName)
            : newJCas.createView(viewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      new ViewCopier().migrate(view, newView);
    }
  }

  /**
   * {@inheritDoc}
   * <p>It will be true iff we haven't returned the new view we create.</p>
   */
  @Override
  public boolean hasNext() throws AnalysisEngineProcessException {
    return !returned;
  }

  /**
   * {@inheritDoc}
   * <p>Only returns the one new view created.</p>
   */
  @Override
  public AbstractCas next() throws AnalysisEngineProcessException {
    returned = true;
    return newJCas;
  }
}
