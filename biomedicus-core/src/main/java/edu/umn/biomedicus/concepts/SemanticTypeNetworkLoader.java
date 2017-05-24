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

package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class SemanticTypeNetworkLoader extends DataLoader<SemanticTypeNetwork> {

    private final Path srdefPath;

    private final Path semgroupsPath;

    @Inject
    public SemanticTypeNetworkLoader(@Setting("semanticNetwork.srdef.path") Path srdefPath,
                                     @Setting("semanticNetwork.semgroups.path") Path semgroupsPath) {
        this.srdefPath = srdefPath;
        this.semgroupsPath = semgroupsPath;
    }

    @Override
    protected SemanticTypeNetwork loadModel() throws BiomedicusException {
        try {
            return SemanticTypeNetwork.loadFromFiles(srdefPath, semgroupsPath);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
