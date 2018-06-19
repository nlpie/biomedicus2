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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.measures.BiomedicusMeasuresModule;
import edu.umn.biomedicus.vocabulary.VocabularyModule;
import edu.umn.nlpengine.Systems;
import edu.umn.nlpengine.SystemsModule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Class which loads all biomedicus configuration and creates a Guice injector. Should only be used
 * once per application run.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class Bootstrapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

  private final List<Module> modules = new ArrayList<>();

  @Nullable
  private String home = null;

  @Nullable
  private Injector injector = null;

  @Nullable
  private Map<String, Object> overloadedSettings = null;

  public Bootstrapper() {
    modules.add(new VocabularyModule());
    modules.add(new BiomedicusMeasuresModule());
  }

  /**
   * Creates an instance of the Biomedicus application class using a pre-existing guice injector.
   * Biomedicus will use a child injector of this application.
   *
   * @param injector the pre-existing guice injector
   * @return biomedicus application whose injector is a child of the argument injector
   * @throws BiomedicusException if we fail to load necessary configuration, or a necessary path is
   * undefined
   */
  public static Application create(Injector injector) throws BiomedicusException {
    Bootstrapper bootstrapper = new Bootstrapper();
    bootstrapper.setInjector(injector);
    bootstrapper.initializePathsAndConfiguration();
    return bootstrapper.biomedicus();
  }

  /**
   * Creates an instance of the biomedicus application class with optional overloaded settings and
   * optional additional guice modules.
   *
   * @param overloadedSettings the settings to overload.
   * @param additionalModules the additional guice modules to add.
   * @return biomedicus application class
   * @throws BiomedicusException if we fail to load necessary configuration, or a necessary path is
   * undefined
   */
  public static Application create(Map<String, Object> overloadedSettings,
      Module... additionalModules)
      throws BiomedicusException {
    Bootstrapper bootstrapper = new Bootstrapper();
    bootstrapper.setOverloadedSettings(overloadedSettings);
    for (Module additionalModule : additionalModules) {
      bootstrapper.addAdditionalModule(additionalModule);
    }
    bootstrapper.initializePathsAndConfiguration();
    return bootstrapper.biomedicus();
  }

  /**
   * Creates an instance of the biomedicus application class with optional additional guice
   * modules.
   *
   * @param additionalModules the additional guice modules to add.
   * @return biomedicus application class
   * @throws BiomedicusException if we fail to load necessary configuration, or a necessary path is
   * undefined
   */
  public static Application create(Module... additionalModules)
      throws BiomedicusException {
    return create(Collections.emptyMap(), additionalModules);
  }

  private Bootstrapper setInjector(Injector injector) {
    this.injector = injector;
    return this;
  }

  private Bootstrapper setOverloadedSettings(Map<String, Object> settings) {
    this.overloadedSettings = settings;
    return this;
  }

  private void addAdditionalModule(Module module) {
    modules.add(module);
  }

  private void initializePathsAndConfiguration() throws BiomedicusException {
    home = System.getProperty("biomedicus.paths.home");
    if (home == null) {
      // retained for backwards compatibility
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
      biomedicusConfiguration = yaml.load(bufferedReader);
    } catch (IOException e) {
      LOGGER.warn("Failed to load configuration, falling back to default configuration file.");
      try (BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(
              getClass().getClassLoader().getResourceAsStream("biomedicusConfiguration.yml")
          )
      )) {
        biomedicusConfiguration = yaml.load(bufferedReader);
      } catch (IOException e1) {
        throw new BiomedicusException(e1);
      }
    }

    // resolve paths
    Map<?, ?> biomedicusPaths = null;
    Object pathsObject = biomedicusConfiguration.get("paths");
    if (pathsObject instanceof Map) {
      biomedicusPaths = (Map) pathsObject;
    }

    // resolve home path from configuration if not already
    if (home == null && biomedicusPaths != null) {
      Object home = biomedicusPaths.get("home");
      if (!(home instanceof String)) {
        throw new BiomedicusException("home path not String: " + home);
      }
      this.home = (String) home;
    }

    String dataEnv = System.getProperty("biomedicus.paths.data");
    if (dataEnv == null) {
      dataEnv = System.getenv("BIOMEDICUS_DATA");
    }
    Path dataPath;
    if (dataEnv != null) {
      dataPath = Paths.get(dataEnv);
    } else {
      if (biomedicusPaths != null && biomedicusPaths.containsKey("data")) {
        String dataDir = (String) biomedicusPaths.get("data");
        dataPath = absoluteOrResolveAgainstHome(Paths.get(dataDir));
      } else {
        dataPath = homePath().resolve("data");
      }
    }

    LOGGER.info("Using data directory: {}", dataPath);

    // collapse settings maps
    SettingsBinder settingsBinder = SettingsBinder.create(dataPath, configDir, homePath());

    SettingsLoader configurationSettingsLoader = new SettingsLoader(biomedicusConfiguration);

    configurationSettingsLoader.loadSettings();
    configurationSettingsLoader.addToBinder(settingsBinder);

    Systems systems = new Systems();
    systems.addSystems(configurationSettingsLoader.getSystemClasses());

    try {
      if (Files.exists(configDir)) {
        Iterator<Path> settingsFilesItr = Files.walk(configDir)
            .filter(path -> path.getFileName().toString()
                .endsWith("Settings.yml"))
            .iterator();
        while (settingsFilesItr.hasNext()) {
          Path settingsFilePath = settingsFilesItr.next();
          SettingsLoader settingsLoader = SettingsLoader
              .createSettingsLoader(settingsFilePath);
          settingsLoader.loadSettings();
          settingsLoader.addToBinder(settingsBinder);
          systems.addSystems(settingsLoader.getSystemClasses());
        }
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    if (overloadedSettings != null) {
      settingsBinder.addSettings(overloadedSettings);
    }

    String overloadFile = System.getProperty("biomedicus.settings.overloads");
    if (overloadFile != null) {
      LOGGER.info("Loading setting overloads: {}", overloadFile);
      SettingsLoader settingsLoader;
      try {
        Path overloadFilePath = Paths.get(overloadFile);
        settingsLoader = SettingsLoader
            .createSettingsLoader(absoluteOrResolveAgainstHome(overloadFilePath));
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
      settingsLoader.loadSettings();
      settingsLoader.addToBinder(settingsBinder);
      systems.addSystems(settingsLoader.getSystemClasses());
    }

    modules.add(new SystemsModule(systems));
    modules.add(new BiomedicusModule());
    modules.add(settingsBinder.createModule());
  }

  private Path absoluteOrResolveAgainstHome(Path path) {
    if (path.isAbsolute()) {
      return path;
    }
    return homePath().resolve(path);
  }

  private Path homePath() {
    if (home == null) {
      throw new IllegalStateException(
          "BioMedICUS home directory is not configured. "
              + "Use the the Java property "
              + "-Dbiomedicus.paths.home=[home dir], or "
              + "set it in the biomedicusConfiguration.yml file "
              + "and set the -Dbiomedicus.paths.conf Java "
              + "property to the directory containing "
              + "biomedicusConfiguration.yml");
    }
    return Paths.get(home);
  }

  private Application biomedicus() {
    Injector biomedicusInjector;
    if (injector != null) {
      biomedicusInjector = injector.createChildInjector(modules);
    } else {
      biomedicusInjector = Guice.createInjector(Stage.PRODUCTION, modules);
    }

    return biomedicusInjector.getInstance(Application.class);
  }
}
