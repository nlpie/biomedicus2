package edu.umn.biomedicus.common.settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 *
 */
public class MapBasedSettings implements Settings {
    private final Map<String, String> settingsMap;

    protected MapBasedSettings(Map<String, String> settingsMap) {
        this.settingsMap = settingsMap;
    }

    @Override
    public String get(String key) {
        if (!settingsMap.containsKey(key)) {
            throw new IllegalArgumentException("Settings does not contain key: " + key);
        }
        return settingsMap.get(key);
    }

    @Override
    public boolean containsSetting(String key) {
        return settingsMap.containsKey(key);
    }

    @Override
    public boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    @Override
    public int getAsInt(String key) {
        return Integer.parseInt(get(key));
    }

    @Override
    public long getAsLong(String key) {
        return Long.parseLong(get(key));
    }

    @Override
    public float getAsFloat(String key) {
        return Float.parseFloat(get(key));
    }

    @Override
    public double getAsDouble(String key) {
        return Double.parseDouble(get(key));
    }

    @Override
    public Path getAsPath(String key) {
        return Paths.get(get(key));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     */
    public static class Builder {
        private final Map<String, String> map;

        public Builder() {
            map = new HashMap<>();
        }

        Builder(Map<String, String> map) {
            this.map = map;
        }

        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Builder put(String key, boolean value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, int value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, long value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, float value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, double value) {
            map.put(key, String.valueOf(value));
            return this;
        }

        public Builder put(String key, Object value) {
            put(Objects.requireNonNull(key, "null key"), Objects.requireNonNull(value, "null value for key " + key).toString());
            return this;
        }

        public Builder loadProperties(Path propertiesFile) throws IOException {
            try (InputStream inputStream = Files.newInputStream(propertiesFile)) {
                Properties properties = new Properties();
                properties.load(inputStream);

                @SuppressWarnings("unchecked")
                Map<String, String> asMap = (Map) properties;
                map.putAll(asMap);
            }
            return this;
        }

        public Settings build() {
            return new MapBasedSettings(map);
        }
    }
}
