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

import java.util.List;
import javax.annotation.Nonnull;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;

/**
 * A type conversion from one UIMA type to another.
 */
public interface TypeConversion {

  @Nonnull
  String sourceTypeName();

  @Nonnull
  List<FeatureStructure> doMigrate(
      @Nonnull CAS sourceView,
      @Nonnull CAS targetView,
      @Nonnull FeatureStructure from,
      @Nonnull Type fromType
  );
}
