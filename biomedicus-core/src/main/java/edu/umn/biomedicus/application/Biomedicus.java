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
import edu.umn.biomedicus.common.types.text.NormIndex;
import edu.umn.biomedicus.common.types.text.WordIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.vocabulary.NormsIndex;
import edu.umn.biomedicus.vocabulary.TermsIndex;
import edu.umn.biomedicus.vocabulary.WordsIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class that provides access to the functionality of Biomedicus. The instances of this class is the
 * "application instance" of Biomedicus.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
@Singleton
public final class Biomedicus {
    private final Injector injector;
    private final Path confFolder;
    private final Path dataFolder;

    @Inject
    Biomedicus(Injector injector,
               @Setting("paths.conf") Path confFolder,
               @Setting("paths.data") Path dataFolder) {
        this.injector = injector;
        this.confFolder = confFolder;
        this.dataFolder = dataFolder;
    }

    public Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    public <T> T getGlobalSetting(Class<T> settingType, String key) {
        return injector.getInstance(Key.get(settingType, new SettingImpl(key)));
    }

    public Path confFolder() {
        return confFolder;
    }

    public Path getConfFolder() {
        return confFolder;
    }

    public Path dataFolder() {
        return dataFolder;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public static final class ViewIdentifiers {
        public static final String ORIGINAL_DOCUMENT = "OriginalDocument";
        public static final String SYSTEM = "SystemView";
        public static final String GOLD_STANDARD = "GoldStandard";
    }

    public static final class Patterns {
        /**
         * Private constructor to prevent instantiation of utility class.
         */
        private Patterns() {
            throw new UnsupportedOperationException();
        }

        /**
         * A Pattern that will match against any character that is not whitespace.
         * <p>
         * Using {@link java.util.regex.Matcher#find} will return whether or not a string has any non-whitespace characters.
         */
        public static final Pattern NON_WHITESPACE = Pattern.compile("\\S");

        /**
         * A Pattern that will match against a string that only contains one or more unicode alphabetic characters.
         */
        public static final Pattern ALPHABETIC_WORD = Pattern.compile("[\\p{L}]+");

        /**
         * A pattern that will match against a string that only contains one or more unicode alphanumeric characters.
         */
        public static final Pattern ALPHANUMERIC_WORD = Pattern.compile("[\\p{L}\\p{Nd}]+");

        /**
         * A pattern that will match any unicode alphanumeric character
         */
        public static final Pattern A_LETTER_OR_NUMBER = Pattern.compile("[\\p{Nd}\\p{L}]");

        /**
         * A pattern that will match the newline character.
         */
        public static final Pattern NEWLINE = Pattern.compile("\n");

        /**
         * Loads a pattern from a file in the resource path by joining all of the lines of the file with an OR symbol '|'
         *
         * @param resourceName the path to the resource of regex statements to be joined
         * @return newly created pattern
         */
        public static Pattern loadPatternByJoiningLines(String resourceName) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(resourceName)))) {
                return getPattern(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static Pattern getPattern(BufferedReader reader) {
            return Pattern.compile(reader.lines().collect(Collectors.joining("|")), Pattern.MULTILINE);
        }

        public static Pattern loadPatternByJoiningLines(Path path) throws BiomedicusException {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return getPattern(reader);
            } catch (IOException e) {
                throw new BiomedicusException("Failed to load pattern.", e);
            }
        }
    }
}
