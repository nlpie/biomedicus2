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

package edu.umn.biomedicus.uima.migration;

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.uima.copying.UimaCopying;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Migrates one type system to another. Works by doing one to one conversions from the type
 * conversions in the designated migration.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class MigratorEngine extends CasMultiplier_ImplBase {

  @Nullable
  private TypeSystemMigration migration;

  private boolean casReturned = false;

  private CAS newCas;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    String migrationClassName = (String) aContext.getConfigParameterValue("migration");
    try {
      migration = Class.forName(migrationClassName).asSubclass(TypeSystemMigration.class)
          .newInstance();
      Preconditions.checkNotNull(migration);
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public boolean hasNext() throws AnalysisEngineProcessException {
    return !casReturned;
  }

  @Override
  public AbstractCas next() throws AnalysisEngineProcessException {
    casReturned = true;
    return newCas;
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    assert migration != null : "migration should never be null at this point";

    TypeSystem typeSystem = aCAS.getTypeSystem();

    newCas = getEmptyCAS();
    casReturned = false;

    migration.setupDocument(aCAS, newCas);
    boolean deleteByDefault = migration.deleteByDefault();
    List<String> typesNotDefaulted = migration.typesNotDefaulted();

    for (Pair<String, String> viewMigration : migration.viewMigrations()) {
      String fromViewName = viewMigration.first();
      String toViewName = viewMigration.second();

      CAS fromView;
      if (fromViewName.equals("_InitialView")) {
        fromView = aCAS;
      } else {
        fromView = aCAS.getView(fromViewName);
      }

      CAS toView;
      if (toViewName.equals("_InitialView")) {
        toView = newCas;
      } else {
        toView = newCas.createView(toViewName);
      }

      List<String> converted = new ArrayList<>();

      migration.setupView(fromView, toView);

      List<TypeConversion> typeConversions = migration.getTypeConversions();
      for (TypeConversion typeConversion : typeConversions) {
        String sourceTypeName = typeConversion.sourceTypeName();
        converted.add(sourceTypeName);
        Type sourceType = typeSystem.getType(sourceTypeName);

        FSIndexRepository indexRepository = fromView.getIndexRepository();
        FSIterator<FeatureStructure> allIndexedFS = indexRepository.getAllIndexedFS(sourceType);
        while (allIndexedFS.hasNext()) {
          FeatureStructure from = allIndexedFS.next();

          typeConversion.doMigrate(fromView, toView, from, sourceType);
        }
      }

      FSIterator<FeatureStructure> allFSes = fromView.getIndexRepository()
          .getAllIndexedFS(fromView.getTypeSystem().getType(CAS.TYPE_NAME_TOP));

      while (allFSes.hasNext()) {
        FeatureStructure next = allFSes.next();

        String typeName = next.getType().getName();
        if (converted.contains(typeName)) {
          continue;
        }

        boolean doDefault = !typesNotDefaulted.contains(typeName);
        if ((!deleteByDefault && doDefault) || (deleteByDefault && !doDefault)) {
          UimaCopying.copyFeatureStructure(next, toView);
        }
      }
    }
  }
}
