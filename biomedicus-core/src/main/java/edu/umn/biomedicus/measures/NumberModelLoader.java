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

package edu.umn.biomedicus.measures;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.numbers.NumberModel;
import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class NumberModelLoader extends DataLoader<NumberModel> {

  private final Path nrnumPath;

  private final Path nrvarPath;

  @Inject
  public NumberModelLoader(@Setting("measures.numbers.nrnumPath") Path nrnumPath,
      @Setting("measures.numbers.nrvarPath") Path nrvarPath) {
    this.nrnumPath = nrnumPath;
    this.nrvarPath = nrvarPath;
  }

  @Override
  protected NumberModel loadModel() throws BiomedicusException {
    try {
      return NumberModel.createNumberModel(nrnumPath, nrvarPath);
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }
}
