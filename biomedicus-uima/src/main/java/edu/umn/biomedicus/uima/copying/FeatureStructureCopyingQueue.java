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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;

/**
 * Responsible for copying the FeatureStructures of one UIMA CAS view to another. Has a queue and
 * map which tracks the current progress iterating through all FeatureStructures.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class FeatureStructureCopyingQueue {

  /**
   * A class for assisting in copying {@code FeatureStructure}s to another.
   */
  private final FsCopiers fsCopiers;

  /**
   * The class for assisting in creating new {@code FeatureStructure}s.
   */
  private final FsConstructors fsConstructors;

  /**
   * A queue of all the {@code FeatureStructure}s that have yet to be processed.
   */
  private final Deque<FeatureStructure> fsQueue;

  /**
   * A map from {@code FeatureStructure}s in the old view to their counterpart in the new view.
   */
  private final Map<FeatureStructure, FeatureStructure> fsMap;

  /**
   * Constructor which takes the fields of this class as parameters.
   *
   * @param fsCopiers class for assisting with copying feature structures
   * @param fsConstructors class for assisting with creating new feature structures
   * @param fsQueue queue for tracking feature structures that need to be processes.
   * @param fsMap map from feature structures in the source CAS to feature structures in the
   * destination CAS
   */
  FeatureStructureCopyingQueue(FsCopiers fsCopiers,
      FsConstructors fsConstructors,
      Deque<FeatureStructure> fsQueue,
      Map<FeatureStructure, FeatureStructure> fsMap) {
    this.fsCopiers = fsCopiers;
    this.fsConstructors = fsConstructors;
    this.fsQueue = fsQueue;
    this.fsMap = fsMap;
  }

  /**
   * Constructor which takes the destination view. Initializes the {@code FsCopiers} and {@code
   * FsConstructors} fields using new objects. Initializes the queue using {@link ArrayDeque} and
   * the map using {@link HashMap}.
   *
   * @param destinationView the
   */
  FeatureStructureCopyingQueue(CAS sourceView, CAS destinationView) {
    fsCopiers = new FsCopiers(this::enqueue);
    fsConstructors = new FsConstructors(destinationView);
    fsQueue = new ArrayDeque<>();
    fsMap = new HashMap<>();
    fsMap.put(sourceView.getSofa(), destinationView.getSofa());
  }

  /**
   * Queues the {@code FeatureStructure} for processing.
   *
   * @param featureStructure FeatureStructure in the source document.
   * @return {@code FeatureStructure} to copy source data to
   */
  FeatureStructure enqueue(FeatureStructure featureStructure) {
    Objects.requireNonNull(featureStructure);
    if (fsMap.containsKey(featureStructure)) {
      return fsMap.get(featureStructure);
    }
    fsQueue.add(featureStructure);
    FeatureStructure targetFs = fsConstructors.createNewInstanceOfSameType(featureStructure);
    fsMap.put(featureStructure, targetFs);
    return targetFs;
  }

  /**
   * Processes all of the queued feature structures.
   */
  void run() {
    while (!fsQueue.isEmpty()) {
      FeatureStructure from = fsQueue.poll();
      FeatureStructure to = fsMap.get(from);
      fsCopiers.copy(from, to);
    }
  }
}
