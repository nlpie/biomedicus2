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
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@ProvidedBy(HistoryModificationModel.Loader.class)
final class HistoryModificationModel {
    private final ContextCues contextCues;

    private HistoryModificationModel(ContextCues contextCues) {
        this.contextCues = contextCues;
    }

    ContextCues getContextCues() {
        return contextCues;
    }

    @Singleton
    static final class Loader extends DataLoader<HistoryModificationModel> {

        private final Path historyModificationSettingsFile;

        @Inject
        public Loader(@Setting("modification.history.path") Path historyModificationSettingsFile) {
            this.historyModificationSettingsFile = historyModificationSettingsFile;
        }

        @Override
        protected HistoryModificationModel loadModel() throws BiomedicusException {
            try {
                ContextCues contextCues = ContextCues.deserialize(historyModificationSettingsFile);
                return new HistoryModificationModel(contextCues);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
