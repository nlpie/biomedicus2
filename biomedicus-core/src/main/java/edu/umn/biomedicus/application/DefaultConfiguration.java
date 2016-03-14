package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.settings.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
class DefaultConfiguration implements BiomedicusConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Settings settings;

    private final Path biomedicusHomeDir;

    private final Path configDir;

    private final Path dataDir;

    DefaultConfiguration() throws IOException {
        String home = System.getProperty("biomedicus.path.home");
        if (home == null) {
            home = System.getenv("BIOMEDICUS_HOME");
        }
        if (home == null) {
            throw new IllegalStateException("BioMedICUS home directory is not configured. Use either the" +
                    " BIOMEDICUS_HOME environment variable or the Java property -Dbiomedicus.path.home=[home dir].");
        }
        biomedicusHomeDir = Paths.get(home).normalize();

        LOGGER.info("Using home directory: {}", biomedicusHomeDir);

        String conf = System.getProperty("biomedicus.path.conf");
        Path configDir;
        if (conf == null) {
            conf = System.getenv("BIOMEDICUS_CONF");
        }

        if (conf != null) {
            configDir = absoluteOrResolveAgainstHome(Paths.get(conf));
        } else {
            configDir = biomedicusHomeDir.resolve("config");
        }
        LOGGER.info("Using configuration directory: {}", configDir);
        this.configDir = configDir;

        Path biomedicusProperties = configDir.resolve("biomedicus.properties");
        Settings.Builder builder = Settings.builder().loadProperties(biomedicusProperties);

        Properties properties = System.getProperties();

        for (Map.Entry<Object, Object> propertyEntry : properties.entrySet()) {
            String key = (String) propertyEntry.getKey();
            if (key.startsWith("biomedicus.")) {
                builder.put(key.replaceFirst("\\Abiomedicus\\.", ""), (String) propertyEntry.getValue());
            }
        }

        settings = builder.build();

        String dataEnv = System.getenv("BIOMEDICUS_DATA");
        if (dataEnv != null) {
            dataDir = Paths.get(dataEnv);
        } else {
            if (settings.containsSetting("path.data")) {
                dataDir = absoluteOrResolveAgainstHome(settings.getAsPath("path.data"));
            } else {
                dataDir = biomedicusHomeDir.resolve("data");
            }
        }

        LOGGER.info("Using data directory: {}", dataDir);
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public Path getDataDir() {
        return dataDir;
    }

    @Override
    public Path getConfigDir() {
        return configDir;
    }

    private Path absoluteOrResolveAgainstHome(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return biomedicusHomeDir.resolve(path);
    }
}
