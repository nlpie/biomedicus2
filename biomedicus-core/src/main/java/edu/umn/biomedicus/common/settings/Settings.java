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

package edu.umn.biomedicus.common.settings;

import java.nio.file.Path;
import java.util.*;

public class Settings {
    private final Map<String, Object> backingMap = new HashMap<>();

    public Settings(Map<String, Object> settingsMap) {
        backingMap.putAll(settingsMap);
    }

    public Optional<?> get(String key) {
        if (!backingMap.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(backingMap.get(key));
    }

    public Optional<String> getString(String key) {
        return get(key).filter(o -> o instanceof String).map(o -> (String) o);
    }

    public Optional<Path> getPath(String key) {
        return get(key).filter(o -> o instanceof Path).map(o -> (Path) o);
    }

    public OptionalLong getLong(String key) {
        Optional<?> opt = get(key);
        if (opt.isPresent()) {
            Object obj = opt.get();
            if (obj instanceof Long) {
                return OptionalLong.of(((Long) obj));
            }
            if (obj instanceof Integer) {
                return OptionalLong.of(((Integer) obj).longValue());
            }
            if (obj instanceof Short) {
                return OptionalLong.of(((Short) obj).longValue());
            }
            if (obj instanceof Byte) {
                return OptionalLong.of(((Byte) obj).longValue());
            }
        }
        return OptionalLong.empty();
    }

    /**
     * Returns the setting with the specified key as a
     *
     * @param key
     * @throws ArithmeticException if the value exists in settings as a long and can't be narrowed to int.
     * @return
     */
    public OptionalInt getInt(String key) {
        Optional<?> opt = get(key);
        if (opt.isPresent()) {
            Object obj = opt.get();
            if (obj instanceof Long) {
                return OptionalInt.of(Math.toIntExact((Long) obj));
            }
            if (obj instanceof Integer) {
                return OptionalInt.of(((Integer) obj));
            }
            if (obj instanceof Short) {
                return OptionalInt.of(((Short) obj).intValue());
            }
            if (obj instanceof Byte) {
                return OptionalInt.of(((Byte) obj).intValue());
            }
        }
        return OptionalInt.empty();
    }


}
