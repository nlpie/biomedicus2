package edu.umn.biomedicus.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.application.BiomedicusModule.NamedImplementationBinding;
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

    private final Path dataPath;
    private final Map<String, String> settingInterfaces;
    private final Map<String, Map<String, String>> interfaceImplementations;
    private final Collection<NamedImplementationBinding> namedImplementationBindings;

    private String home;
    private final Map<String, Object> settings;

    public Bootstrapper(Module... additionalModules) throws BiomedicusException {
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
        Map<String, String> settingInterfaces =  (Map<String, String>) biomedicusConfiguration.get("settingInterfaces");
        this.settingInterfaces = settingInterfaces;

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> interfaceImplementations = (Map<String, Map<String, String>>) biomedicusConfiguration.get("interfaceImplementations");
        this.interfaceImplementations = interfaceImplementations;

        namedImplementationBindings = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : interfaceImplementations.entrySet()) {
            String superClassName = entry.getKey();
            Map<String, String> implementations = entry.getValue();
            for (Map.Entry<String, String> implementationEntry : implementations.entrySet()) {
                String key = implementationEntry.getKey();
                String className = implementationEntry.getValue();
                addBinding(superClassName, key, className);
            }

        }

        interfaceImplementations.forEach((superclassClassName, implementations) -> {
            implementations.forEach((key, className) -> {

            });
        });

        // collapse settings maps
        settings = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> settingsYaml = (Map<String, Object>) biomedicusConfiguration.get("settings");
        recursiveAddSettings(settingsYaml, null);

        modules.add(new BiomedicusModule(settings, namedImplementationBindings));
        modules.addAll(Arrays.asList(additionalModules));

        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
    }

    private void addBinding(String superClassName, String key, String className) throws BiomedicusException {
        Class<?> superclass;
        Class<?> aClass;
        try {
            superclass = Class.forName(superClassName);
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BiomedicusException("Bound class not found.", e);
        }
        namedImplementationBindings.add(NamedImplementationBinding.create(superclass, key, aClass));
    }

    private void recursiveAddSettings(Map<String, Object> yaml, String prevKey) throws BiomedicusException {
        for (Map.Entry<String, Object> settingEntry : yaml.entrySet()) {
            String entryKey = settingEntry.getKey();
            String key = prevKey == null ? entryKey : prevKey + "." + entryKey;
            Object value = settingEntry.getValue();

            String superclassClassName = settingInterfaces.get(key);
            if (superclassClassName != null) {
                String className = interfaceImplementations.get(superclassClassName).get(value);
                addBinding(superclassClassName, key, className);
            }

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value;
                recursiveAddSettings(valueMap, key);
            } else if (key.endsWith(".path")) {
                Path path = absoluteOrResolveAgainstData(Paths.get((String) value));
                settings.put(key, path);
            } else {
                settings.put(key, value);
            }
        }

    }

    private Path absoluteOrResolveAgainstData(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return dataPath.resolve(path);
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

    public static Bootstrapper create(Module... additionalModules) throws BiomedicusException {
        return new Bootstrapper(additionalModules);
    }

    public Injector injector() {
        return injector;
    }

    public <T> T getInstance(Class<T> tClass) {
        return injector.getInstance(tClass);
    }
}
