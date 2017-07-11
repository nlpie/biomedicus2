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

package edu.umn.biomedicus.framework;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import java.nio.file.Path;

/**
 * A class that provides access to the functionality of Biomedicus. The instances of this class is
 * the "application instance" of Biomedicus.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
@Singleton
public final class Application {

  private final Injector injector;
  private final Path confFolder;
  private final Path dataFolder;

  @Inject
  Application(Injector injector,
      @Setting("paths.conf") Path confFolder,
      @Setting("paths.data") Path dataFolder) {
    this.injector = injector;
    this.confFolder = confFolder;
    this.dataFolder = dataFolder;
  }

  public Injector getInjector() {
    return injector;
  }

  public <T> T getInstance(Class<T> tClass) {
    return injector.getInstance(tClass);
  }

  public <T> T getGlobalSetting(Class<T> settingType, String key) {
    return injector.getInstance(Key.get(settingType, new SettingImpl(key)));
  }

  public Path confFolder() {
    return confFolder;
  }

  public Path getConfFolder() {
    return confFolder;
  }

  public Path dataFolder() {
    return dataFolder;
  }

  public Path getDataFolder() {
    return dataFolder;
  }
}
