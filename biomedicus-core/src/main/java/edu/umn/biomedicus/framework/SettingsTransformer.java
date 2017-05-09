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

package edu.umn.biomedicus.framework;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Named;
import edu.umn.biomedicus.annotations.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
class SettingsTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsTransformer.class);
    private final Map<String, Class<?>> settingInterfaces;
    private final Path dataPath;
    private final Map<Key<?>, Object> settings;
    private Function<String, Annotation> annotationFunction;

    @Inject
    SettingsTransformer(@Named("settingInterfaces") Map<String, Class<?>> settingInterfaces,
                        @Setting("paths.data") Path dataPath) {
        this.settingInterfaces = settingInterfaces;
        this.dataPath = dataPath;
        settings = new HashMap<>();
    }

    void setAnnotationFunction(Function<String, Annotation> annotationFunction) {
        this.annotationFunction = annotationFunction;
    }

    void addAll(Map<String, Object> settingsMap) {
        recursiveAddSettings(settingsMap, null);
    }

    Map<Key<?>, Object> getSettings() {
        return settings;
    }

    private void recursiveAddSettings(Map<String, Object> settingsMap, String prevKey) {
        for (Map.Entry<String, Object> settingEntry : settingsMap.entrySet()) {
            String entryKey = settingEntry.getKey();
            String key = prevKey == null ? entryKey : prevKey + "." + entryKey;
            Object value = settingEntry.getValue();

            Class<?> interfaceClass = settingInterfaces.get(key);
            if (interfaceClass != null) {
                addSettingImplementation(interfaceClass, key, (String) value);
            }

            if (value == null) {
                LOGGER.info("Null setting: {}", key);
                continue;
            }

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value;
                recursiveAddSettings(valueMap, key);
            } else if (key.endsWith(".path")) {
                Path path = absoluteOrResolveAgainstData(Paths.get((String) value));
                settings.putIfAbsent(Key.get(Path.class, annotationFunction.apply(key)), path);
            } else {
                addSetting(key, value, value.getClass());
            }
        }
    }

    private <T> void addSettingImplementation(Class<T> interfaceClass, String settingKey, String implementationKey) {
        Key<T> key = Key.get(interfaceClass, annotationFunction.apply(settingKey));
        Key<T> value = Key.get(interfaceClass, new SettingImpl(implementationKey));
        settings.putIfAbsent(key, value);
    }

    private <T> void addSetting(String key, Object value, Class<T> valueClass) {
        settings.putIfAbsent(Key.get(valueClass, annotationFunction.apply(key)), value);
    }

    private Path absoluteOrResolveAgainstData(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return dataPath.resolve(path);
    }
}
