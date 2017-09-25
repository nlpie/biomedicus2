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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.measures.UnitRecognizer.Factory;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.Numbers;
import java.io.IOException;

public class MeasuresModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(NumberModel.class).toProvider(NumberModelLoader.class).in(Scopes.SINGLETON);

    bind(UnitRecognizer.Factory.class).toProvider(UnitsFactoryLoader.class).in(Scopes.SINGLETON);
    bind(UnitRecognizer.class).toProvider(UnitRecognizerProvider.class);
  }

  @Singleton
  private static final class UnitRecognizerProvider implements Provider<UnitRecognizer> {
    private final Factory factory;

    @Inject
    public UnitRecognizerProvider(Factory factory) {
      this.factory = factory;
    }

    @Override
    public UnitRecognizer get() {
      return factory.create();
    }
  }

  private static final class UnitsFactoryLoader extends DataLoader<UnitRecognizer.Factory> {
    @Override
    protected Factory loadModel() throws BiomedicusException {
      try {
        return UnitRecognizer.createFactory();
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
