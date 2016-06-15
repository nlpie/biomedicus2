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

package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.common.grams.Ngram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Spelling model based off the SPECIALIST lexicon LRSPL file.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
@Singleton
public class SpecialistSpellingModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialistSpellingModel.class);

    private final Map<String, String> dictionary;

    @Inject
    public SpecialistSpellingModel(@Setting("specialist.path") Path specialistPath) throws IOException {
        Path lrsplPath = specialistPath.resolve("LRSPL");

        LOGGER.info("Loading SPECIALIST LRSPL dictionary from path: {}", lrsplPath);
        dictionary = Files.lines(lrsplPath).map(Pattern.compile("\\|")::split)
                .map(columns -> {
                    String variant = columns[1];
                    String canonical = columns[2];
                    return Ngram.create(variant, canonical);
                })
                .collect(Collectors.toMap(Bigram::getFirst, Bigram::getSecond, (String first, String second) -> {
                    if (!Objects.equals(first, second)) {
                        throw new IllegalStateException(String.format("Duplicate key mapping to different values: %s, %s", first, second));
                    }
                    return first;
                }));
    }

    public String getCanonicalForm(String variant) {
        return dictionary.get(variant);
    }
}
