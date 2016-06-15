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

package edu.umn.biomedicus.modification;


import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

@ProvidedBy(NegationModificationModel.Loader.class)
final class NegationModificationModel {
    private final ContextCues contextCues;

    private NegationModificationModel(ContextCues contextCues) {
        this.contextCues = contextCues;
    }

    ContextCues getContextCues() {
        return contextCues;
    }

    @Singleton
    static final class Loader extends DataLoader<NegationModificationModel> {

        private final Path negationSettingsPath;

        @Inject
        public Loader(@Setting("modification.negation.path") Path negationSettingsPath) {
            this.negationSettingsPath = negationSettingsPath;
        }

        @Override
        protected NegationModificationModel loadModel() throws BiomedicusException {
            try {
                ContextCues contextCues = ContextCues.deserialize(negationSettingsPath);
                return new NegationModificationModel(contextCues);
            } catch (IOException e) {
                throw new BiomedicusException();
            }
        }
    }
}
