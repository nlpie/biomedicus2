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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;

import java.nio.file.Path;

/**
 *
 */
public class GlobalSettings {
    private final Injector injector;

    @Inject
    public GlobalSettings(Injector injector) {
        this.injector = injector;
    }

    private <T> T getSetting(String key, Class<T> tClass) {
        Key<T> guiceKey = Key.get(tClass, new SettingImpl(key));
        return injector.getInstance(guiceKey);
    }

    public <T> T getInstance(String key, Class<T> tClass) {
        String implementationKey = injector.getInstance(Key.get(String.class, new SettingImpl(key)));
        return getSetting(key, tClass);
    }

    public String getString(String key) {
        return getSetting(key, String.class);
    }

    public Integer getInteger(String key) {
        return getSetting(key, Integer.class);
    }

    public Path getPath(String key) {
        return getSetting(key, Path.class);
    }

    public Double getSetting(String key) {
        return getSetting(key, Double.class);
    }
}
