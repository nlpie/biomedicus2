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

import com.google.inject.Key;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

class SettingsLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsLoader.class);

  private final Map<String, Object> configurations = new HashMap<>();

  @Nullable
  private Map<String, Class<?>> settingInterfaces;

  @Nullable
  private Map<Class<?>, Map<String, Class<?>>> interfaceImplementations;

  private final Map<String, Object> settings = new HashMap<>();

  private List<String> systemClasses = new ArrayList<>();

  public SettingsLoader(Map<String, Object> configurations) {
    this.configurations.putAll(configurations);
  }

  static SettingsLoader createSettingsLoader(Path settingsFilePath) throws IOException {
    try (BufferedReader bufferedReader = Files.newBufferedReader(settingsFilePath)) {
      return new SettingsLoader(new Yaml().load(bufferedReader));
    }
  }

  void loadSettings() throws BiomedicusException {
    Object systems = configurations.get("systems");
    if (systems instanceof List) {
      List systemsList = (List) systems;
      for (Object o : systemsList) {
        if (o instanceof String) {
          systemClasses.add(((String) o));
        }
      }
    }

    Object settingInterfacesYaml = configurations.get("settingInterfaces");
    if (settingInterfacesYaml instanceof Map) {
      settingInterfaces = getClassMap((Map<?, ?>) settingInterfacesYaml);
    }

    Object implementationsYaml = configurations.get("interfaceImplementations");
    if (implementationsYaml instanceof Map) {
      interfaceImplementations = new HashMap<>();
      for (Map.Entry<?, ?> interfaceEntry : ((Map<?, ?>) implementationsYaml).entrySet()) {
        try {
          Object className = interfaceEntry.getKey();
          Object interfaceMap = interfaceEntry.getValue();
          if (className instanceof String && interfaceMap instanceof Map) {
            Class<?> interfaceClass = Class.forName((String) className);
            Map<?, ?> interfaceMapYaml = (Map) interfaceMap;
            Map<String, Class<?>> interfaces = getClassMap(interfaceMapYaml);
            interfaceImplementations.put(interfaceClass, interfaces);
          }
        } catch (ClassNotFoundException e) {
          throw new BiomedicusException(e);
        }
      }
    }

    Object settings = configurations.get("settings");
    if (settings instanceof Map) {
      recursiveAddSettings((Map<?, ?>) settings, null);
    }
  }

  private void recursiveAddSettings(Map<?, ?> settingsMap, @Nullable String prevKey) {
    for (Map.Entry<?, ?> settingEntry : settingsMap.entrySet()) {
      Object keyObject = settingEntry.getKey();
      if (!(keyObject instanceof String)) {
        throw new IllegalStateException("Non-String key in settings");
      }
      String entryKey = (String) keyObject;
      String key = prevKey == null ? entryKey : prevKey + "." + entryKey;
      Object value = settingEntry.getValue();
      if (value == null) {
        LOGGER.debug("Null setting: {}", key);
        continue;
      }

      if (value instanceof Map) {
        recursiveAddSettings((Map<?, ?>) value, key);
      } else {
        settings.put(key, value);
      }
    }
  }

  private Map<String, Class<?>> getClassMap(
      Map<?, ?> settingInterfacesYaml
  ) throws BiomedicusException {
    Map<String, Class<?>> settingInterfaces = new HashMap<>();
    for (Map.Entry<?, ?> entry : settingInterfacesYaml.entrySet()) {
      try {
        Object key = entry.getKey();
        Object className = entry.getValue();
        if (key instanceof String && className instanceof String) {
          Class<?> aClass = Class.forName((String) className);
          settingInterfaces.put((String) key, aClass);
        } else {
          LOGGER.warn("Key or value not String in settingInterfaces map. Key: {}. Value: {}",
              key, className);
        }
      } catch (ClassNotFoundException e) {
        throw new BiomedicusException(e);
      }
    }
    return settingInterfaces;
  }

  void addToBinder(SettingsBinder settingsBinder) {
    settingsBinder.addSettings(settings);
  }

  List<String> getSystemClasses() {
    return systemClasses;
  }
}
