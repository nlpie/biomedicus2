package edu.umn.biomedicus.application;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger();

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
        Path configDir;
        if (conf == null) {
            conf = System.getenv("BIOMEDICUS_CONF");
        }

        if (conf != null) {
            configDir = absoluteOrResolveAgainstHome(Paths.get(conf));
        } else {
            if (home == null) {
                throw new IllegalStateException("BioMedICUS home directory is not configured. Use either the" +
                        " BIOMEDICUS_HOME environment variable or the Java property -Dbiomedicus.path.home=[home dir].");
            }
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

        @SuppressWarnings("unchecked")
        Map<String, String> settingInterfacesYaml =  (Map<String, String>) biomedicusConfiguration.get("settingInterfaces");
        Map<String, Class<?>> settingInterfaces = getClassMap(settingInterfacesYaml);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> interfaceImplementationsYaml = (Map<String, Map<String, String>>) biomedicusConfiguration.get("interfaceImplementations");
        Map<Class<?>, Map<String, Class<?>>> interfaceImplementations = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> interfaceEntry : interfaceImplementationsYaml.entrySet()) {
            try {
                Class<?> interfaceClass = Class.forName(interfaceEntry.getKey());
                Map<String, String> interfaceMapYaml = interfaceEntry.getValue();
                Map<String, Class<?>> interfaceMap = getClassMap(interfaceMapYaml);
                interfaceImplementations.put(interfaceClass, interfaceMap);
            } catch (ClassNotFoundException e) {
                throw new BiomedicusException(e);
            }
        }

        // collapse settings maps
        @SuppressWarnings("unchecked")
        Map<String, Object> settingsYaml = (Map<String, Object>) biomedicusConfiguration.get("settings");

        SettingsBinder settingsBinder = new SettingsBinder(settingInterfaces, dataPath, configDir, homePath());
        settingsBinder.setSettingsMap(settingsYaml);
        settingsBinder.setInterfaceImplementations(interfaceImplementations);

        modules.add(new BiomedicusModule());
        modules.add(settingsBinder.createModule());
        modules.addAll(Arrays.asList(additionalModules));

        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
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

    private Path absoluteOrResolveAgainstHome(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return homePath().resolve(path);
    }

    private Path homePath() {
        if (home == null) {
            throw new IllegalStateException("BioMedICUS home directory is not configured. Use the" +
                    " BIOMEDICUS_HOME environment variable, the Java property -Dbiomedicus.path.home=[home dir], or " +
            "set it in the biomedicusConfiguration.yml file and set BIOMEDICUS_CONF environment variable or the " +
            "-Dbiomedicus.paths.conf Java property to the directory containing biomedicusConfiguration.yml");
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
