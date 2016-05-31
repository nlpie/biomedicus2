package edu.umn.biomedicus.application;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsBuilder {
    private final Map<String, Object> settingsMap = new HashMap<>();

    private final Map<String, Class<?>> settingsInterfaces = new HashMap<>();

    private final Map<Class<?>, Collection<String>> interfaceOptions = new HashMap<>();

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    void addConfiguration(Path configFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            Map<String, Object> configMap = (Map<String, Object>) yaml.load(reader);
            Map<String, Object> settings = (Map<String, Object>) configMap.get("settings");
            addSettings(settings);
        }
    }

    void addSettings(Path settingsFile) {

    }

    void addSettings(Map<String, Object> yamlSettings) {

    }

    @SuppressWarnings("unchecked")
    void recursiveAddSettings(Map<String, Object> yamlSettings, String prev) {
        yamlSettings.forEach((key, value) -> {
            String fullKey = prev + "." + key;
            if (value instanceof Map) {
                recursiveAddSettings((Map<String, Object>) value, fullKey);
            } else if (fullKey.endsWith(".path")) {
                settingsMap.put(fullKey, Paths.get((String) value));
            } else {
                settingsMap.put(fullKey, value);
            }
        });
    }
}
