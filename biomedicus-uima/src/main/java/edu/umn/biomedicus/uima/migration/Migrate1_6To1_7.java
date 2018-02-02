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

package edu.umn.biomedicus.uima.migration;

import com.google.inject.Singleton;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.uima.util.FsAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

/**
 * Migrates the 1.6.0 type system to the 1.7.0 type system.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class Migrate1_6To1_7 implements TypeSystemMigration {

  @Override
  public List<TypeConversion> getTypeConversions() {
    return Collections.singletonList(new TypeConversion() {

      @Override
      public String sourceTypeName() {
        return "edu.umn.biomedicus.uima.type1_6.NormForm";
      }

      @Override
      public List<FeatureStructure> doMigrate(CAS sourceView, CAS targetView, FeatureStructure from,
          Type fromType) {
        AnnotationFS fromAnnotation = (AnnotationFS) from;
        FsAccessor fromAccessor = FsAccessor.create(fromAnnotation);
        TypeSystem typeSystem = sourceView.getTypeSystem();

        Type normIndexType = typeSystem
            .getType("edu.umn.biomedicus.uima.type1_5.NormIndex");

        AnnotationIndex<AnnotationFS> normIndexIndex = sourceView
            .getAnnotationIndex(normIndexType);

        FSIterator<AnnotationFS> subiterator = normIndexIndex.subiterator(fromAnnotation);

        if (!subiterator.hasNext()) {
          throw new IllegalStateException("No norm index for a norm form");
        }

        AnnotationFS next = subiterator.next();
        FsAccessor normIndexAccessor = new FsAccessor(next);

        Type newNormFormType = typeSystem
            .getType("edu.umn.biomedicus.uima.type1_7.NormForm");

        AnnotationFS newNormForm = targetView.createAnnotation(newNormFormType,
            fromAnnotation.getBegin(), fromAnnotation.getEnd());
        FsAccessor newAccessor = FsAccessor.create(newNormForm);
        newAccessor.setFeatureValue("index", normIndexAccessor.getIntValue("index"));
        newAccessor.setFeatureValue("normForm", fromAccessor.getStringValue("normForm"));
        targetView.addFsToIndexes(newNormForm);

        return Collections.singletonList(newNormForm);
      }
    });
  }

  @Override
  public boolean deleteByDefault() {
    return false;
  }

  @Override
  public List<String> typesNotDefaulted() {
    return Collections.singletonList("edu.umn.biomedicus.uima.type1_5.NormIndex");
  }

  @Override
  public void setupView(CAS fromView, CAS toView) {

  }

  @Override
  public void setupDocument(CAS oldCAS, CAS newCAS) {

  }

  @Override
  public List<Pair<String, String>> viewMigrations() {
    return Arrays.asList(Pair.of("metadata", "metadata"), Pair.of("SystemView", "SystemView"));
  }
}
