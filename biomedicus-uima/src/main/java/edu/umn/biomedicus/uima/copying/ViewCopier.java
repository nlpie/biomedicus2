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

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

/**
 * Responsible for creating a new view in a parent CAS and then copying all FSes from an existing view to this new view.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class ViewCopier implements ViewMigrator {
    @Override
    public void migrate(JCas source, JCas target) {
        target.setDocumentText(source.getDocumentText());

        FeatureStructureCopyingQueue featureStructureCopyingQueue = new FeatureStructureCopyingQueue(source.getCas(),
                target.getCas());

        FSIterator<FeatureStructure> allFs = source.getIndexRepository().getAllIndexedFS(source.getCasType(TOP.type));
        while (allFs.hasNext()) {
            featureStructureCopyingQueue.enqueue(allFs.next());
        }
        featureStructureCopyingQueue.run();
    }
}
