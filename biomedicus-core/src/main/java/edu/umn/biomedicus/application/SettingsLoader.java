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

package edu.umn.biomedicus.application;

import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class SettingsLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsLoader.class);
    private final Path settingsFilePath;
    private final Yaml yaml;
    @Nullable private Map<String, Class<?>> settingInterfaces;
    @Nullable private Map<Class<?>, Map<String, Class<?>>> interfaceImplementations;
    private Map<String, Object> settings;

    private SettingsLoader(Path settingsFilePath, Yaml yaml) {
        this.settingsFilePath = settingsFilePath;
        this.yaml = yaml;
    }

    static SettingsLoader createSettingsLoader(Path settingsFilePath) {
        return new SettingsLoader(settingsFilePath, new Yaml());
    }

    @SuppressWarnings("unchecked")
    void loadSettings() throws BiomedicusException {
        Map<String, Object> settingsFileYaml;
        try (BufferedReader bufferedReader = Files.newBufferedReader(settingsFilePath)) {
            settingsFileYaml = (Map<String, Object>) yaml.load(bufferedReader);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }

        Map<String, String> settingInterfacesYaml = (Map<String, String>) settingsFileYaml.get("settingInterfaces");
        if (settingInterfacesYaml != null) {
            settingInterfaces = getClassMap(settingInterfacesYaml);
        }

        Map<String, Map<String, String>> implementationsYaml;
        implementationsYaml = (Map<String, Map<String, String>>) settingsFileYaml.get("interfaceImplementations");
        if (implementationsYaml != null) {
            interfaceImplementations = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> interfaceEntry : implementationsYaml.entrySet()) {
                try {
                    Class<?> interfaceClass = Class.forName(interfaceEntry.getKey());
                    Map<String, String> interfaceMapYaml = interfaceEntry.getValue();
                    Map<String, Class<?>> interfaceMap = getClassMap(interfaceMapYaml);
                    interfaceImplementations.put(interfaceClass, interfaceMap);
                } catch (ClassNotFoundException e) {
                    throw new BiomedicusException(e);
                }
            }
        }

        settings = ((Map<String, Object>) settingsFileYaml.get("settings"));
        if (settings == null) {
            throw new BiomedicusException("Null settings from file: " + settingsFilePath);
        }
    }

    private Map<String, Class<?>> getClassMap(Map<String, String> settingInterfacesYaml) throws BiomedicusException {
        Map<String, Class<?>> settingInterfaces = new HashMap<>();
        for (Map.Entry<String, String> entry : settingInterfacesYaml.entrySet()) {
            try {
                Class<?> aClass = Class.forName(entry.getValue());
                settingInterfaces.put(entry.getKey(), aClass);
            } catch (ClassNotFoundException e) {
                throw new BiomedicusException(e);
            }
        }
        return settingInterfaces;
    }

    void addToBinder(SettingsBinder settingsBinder) {
        if (settingInterfaces != null) {
            settingsBinder.addSettingsInterfaces(settingInterfaces);
        }
        if (interfaceImplementations != null) {
            settingsBinder.addInterfaceImplementations(interfaceImplementations);
        }
        settingsBinder.addSettings(settings);
    }
}
