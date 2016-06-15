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

import com.google.inject.*;
import com.google.inject.name.Names;

import java.nio.file.Path;
import java.util.Map;

/**
 *
 */
class SettingsBinder {
    private final SettingsTransformer settingsTransformer;

    private final Map<String, Class<?>> settingInterfaces;

    private final Path dataPath;

    private final Path homePath;

    private final Path confPath;

    private Map<Class<?>, Map<String, Class<?>>> interfaceImplementations;

    private Map<String, Object> settingsMap;

    private Binder binder;

    SettingsBinder(Map<String, Class<?>> settingInterfaces, Path dataPath, Path confPath, Path homePath) {
        this.settingInterfaces = settingInterfaces;
        this.dataPath = dataPath;
        settingsTransformer = new SettingsTransformer(settingInterfaces, dataPath);
        settingsTransformer.setAnnotationFunction(SettingImpl::new);
        this.homePath = homePath;
        this.confPath = confPath;
    }

    static SettingsBinder create(Map<String, Class<?>> settingInterfaces, Path dataPath, Path confPath, Path homePath) {
        return new SettingsBinder(settingInterfaces, dataPath, confPath, homePath);
    }

    void setInterfaceImplementations(Map<Class<?>, Map<String, Class<?>>> interfaceImplementations) {
        this.interfaceImplementations = interfaceImplementations;
    }

    void setSettingsMap(Map<String, Object> settingsMap) {
        this.settingsMap = settingsMap;
    }

    private void performBindings(Binder binder) {
        this.binder = binder;
        if (interfaceImplementations != null) {
            interfaceImplementations.forEach((interfaceClass, implementations) -> {
                implementations.forEach((key, implementation) -> {
                    bindInterfaceImplementation(interfaceClass, key, implementation);
                });
            });

            binder.bind(new TypeLiteral<Map<String, Class<?>>>() {
            }).annotatedWith(Names.named("settingInterfaces"))
                    .toInstance(settingInterfaces);
            binder.bind(Path.class).annotatedWith(new SettingImpl("paths.data")).toInstance(dataPath);
            binder.bind(Path.class).annotatedWith(new SettingImpl("paths.home")).toInstance(homePath);
            binder.bind(Path.class).annotatedWith(new SettingImpl("paths.conf")).toInstance(confPath);
            binder.bind(new TypeLiteral<Map<String, Object>>() {}).annotatedWith(Names.named("globalSettings")).toInstance(settingsMap);
        }
        settingsTransformer.addAll(settingsMap);
        Map<Key<?>, Object> settings = settingsTransformer.getSettings();
        settings.forEach(this::bindSetting);
    }

    private <T> void bindInterfaceImplementation(Class<T> interfaceClass, String key, Class<?> implementation) {
        binder.bind(interfaceClass).annotatedWith(new SettingImpl(key)).to(implementation.asSubclass(interfaceClass));
    }

    private <T> void bindSetting(Key<T> key, Object value) {
        if (value instanceof Key) {
            @SuppressWarnings("unchecked")
            Key<T> valueKey = (Key<T>) value;
            binder.bind(key).to(valueKey);
        } else {
            TypeLiteral<T> typeLiteral = key.getTypeLiteral();
            Class<? super T> rawType = typeLiteral.getRawType();
            @SuppressWarnings("unchecked")
            T cast = (T) rawType.cast(value);
            binder.bind(key).toInstance(cast);
        }
    }

    Module createModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                performBindings(binder());
            }
        };
    }
}
