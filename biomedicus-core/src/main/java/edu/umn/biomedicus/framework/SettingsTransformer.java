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

package edu.umn.biomedicus.framework;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Named;
import edu.umn.biomedicus.annotations.Setting;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes dictionaries of settings (usually loaded from configuration files, and turns them into
 * a Map of Guice keys so that they can be bound for injection.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public class SettingsTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsTransformer.class);

  private final Path dataPath;

  private final Map<Key<?>, Object> settings;

  @Nullable
  private Function<String, Annotation> annotationFunction;

  @Inject
  SettingsTransformer(
      @Setting("paths.data") Path dataPath
  ) {
    this.dataPath = dataPath;
    settings = new HashMap<>();
  }

  void setAnnotationFunction(@Nullable Function<String, Annotation> annotationFunction) {
    this.annotationFunction = annotationFunction;
  }

  /**
   * Adds all of the specified settings to be transformed to Guice keys.
   */
  void addAll(Map<String, ?> settingsMap) {
    Preconditions.checkNotNull(annotationFunction);

    for (Map.Entry<String, ?> settingEntry : settingsMap.entrySet()) {
      String key = settingEntry.getKey();
      Object value = settingEntry.getValue();

      if (value == null) {
        LOGGER.debug("Null setting: {}", key);
        continue;
      }

      if (value instanceof Map) {
        throw new IllegalStateException("Maps should already be collapsed at this point.");
      } else if (value instanceof String && endsWithPathFileDir(key)) {
        Path path = absoluteOrResolveAgainstData(Paths.get((String) value));
        settings.putIfAbsent(Key.get(Path.class, annotationFunction.apply(key)), path);
        settings.putIfAbsent(Key.get(String.class, annotationFunction.apply(key)), path.toString());
        settings.putIfAbsent(Key.get(String.class, annotationFunction.apply(key + ".orig")), value);
      } else {
        addSetting(key, value, value.getClass());
      }
    }
  }

  Map<Key<?>, Object> getSettings() {
    return settings;
  }

  private <T> void addSettingImplementation(Class<T> interfaceClass, String settingKey,
      String implementationKey) {
    assert annotationFunction != null : "checked at entry points";

    Key<T> key = Key.get(interfaceClass, annotationFunction.apply(settingKey));
    Key<T> value = Key.get(interfaceClass, new SettingImpl(implementationKey));
    settings.putIfAbsent(key, value);
  }

  private <T> void addSetting(String key, Object value, Class<T> valueClass) {
    assert annotationFunction != null : "checked at entry points";

    settings.putIfAbsent(Key.get(valueClass, annotationFunction.apply(key)), value);
  }

  private Path absoluteOrResolveAgainstData(Path path) {
    if (path.isAbsolute()) {
      return path;
    }
    return dataPath.resolve(path);
  }

  private boolean endsWithPathFileDir(String key) {
    return key.endsWith("Path") || key.endsWith("path") || key.endsWith("Dir")
        || key.endsWith("Directory") || key.endsWith("File") || key.endsWith("dir")
        || key.endsWith("directory") || key.endsWith("file");
  }
}
