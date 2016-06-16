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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class which loads all biomedicus configuration and creates a Guice injector. Should only be used once per application
 * run.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public class Bootstrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    private final Injector injector;

    private String home;

    private Bootstrapper(Module... additionalModules) throws BiomedicusException {
        List<Module> modules = new ArrayList<>();
        // Load configuration
        home = System.getProperty("biomedicus.paths.home");
        if (home == null) {
            home = System.getenv("BIOMEDICUS_HOME");
        }

        String conf = System.getProperty("biomedicus.paths.conf");
        if (conf == null) {
            conf = System.getenv("BIOMEDICUS_CONF");
        }

        Path configDir;
        if (conf != null) {
            configDir = absoluteOrResolveAgainstHome(Paths.get(conf));
        } else {
            configDir = homePath().resolve("config");
        }

        LOGGER.info("Using configuration directory: {}", configDir);

        Yaml yaml = new Yaml();

        // load configuration
        Path configurationFilePath = configDir.resolve("biomedicusConfiguration.yml");
        Map<String, Object> biomedicusConfiguration;
        try (BufferedReader bufferedReader = Files.newBufferedReader(configurationFilePath)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> configuration = (Map<String, Object>) yaml.load(bufferedReader);
            biomedicusConfiguration = configuration;
        } catch (IOException e) {
            throw new BiomedicusException("Failed to load configuration.", e);
        }

        // resolve paths

        @SuppressWarnings("unchecked")
        Map<String, Object> biomedicusPaths = (Map<String, Object>) biomedicusConfiguration.get("paths");

        // resolve home path from configuration if not already
        if (home == null) {
            home = (String) biomedicusPaths.get("home");
        }

        String dataEnv = System.getProperty("biomedicus.paths.data");
        if (dataEnv == null) {
            dataEnv = System.getenv("BIOMEDICUS_DATA");
        }
        Path dataPath;
        if (dataEnv != null) {
            dataPath = Paths.get(dataEnv);
        } else {
            String dataDir = (String) biomedicusPaths.get("data");
            if (dataDir != null) {
                dataPath = absoluteOrResolveAgainstHome(Paths.get(dataDir));
            } else {
                dataPath = homePath().resolve("data");
            }
        }

        LOGGER.info("Using data directory: {}", dataPath);

        // collapse settings maps
        SettingsBinder settingsBinder = SettingsBinder.create(dataPath, configDir, homePath());
        SettingsLoader configurationSettingsLoader = SettingsLoader.createSettingsLoader(configurationFilePath);
        configurationSettingsLoader.loadSettings();
        configurationSettingsLoader.addToBinder(settingsBinder);
        try {
            Iterator<Path> settingsFilesItr = Files.walk(configDir)
                    .filter(path -> path.getFileName().toString().endsWith("Settings.yml"))
                    .iterator();
            while (settingsFilesItr.hasNext()) {
                Path settingsFilePath = settingsFilesItr.next();
                SettingsLoader settingsLoader = SettingsLoader.createSettingsLoader(settingsFilePath);
                settingsLoader.loadSettings();
                settingsLoader.addToBinder(settingsBinder);
            }
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }

        modules.add(new BiomedicusModule());
        modules.add(settingsBinder.createModule());
        modules.addAll(Arrays.asList(additionalModules));

        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
    }

    private Path absoluteOrResolveAgainstHome(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return homePath().resolve(path);
    }

    private Path homePath() {
        if (home == null) {
            throw new IllegalStateException("BioMedICUS home directory is not configured. Use the" +
                    " the Java property -Dbiomedicus.path.home=[home dir], or " +
                    "set it in the biomedicusConfiguration.yml file and set the -Dbiomedicus.paths.conf Java " +
                    "property to the directory containing biomedicusConfiguration.yml");
        }
        return Paths.get(home);
    }

    public <T> T createClass(Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    public Injector injector() {
        return injector;
    }

    public static Bootstrapper create(Module... additionalModules) throws BiomedicusException {
        return new Bootstrapper(additionalModules);
    }
}
