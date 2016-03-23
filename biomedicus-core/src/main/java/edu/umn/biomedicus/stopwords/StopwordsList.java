/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umn.biomedicus.stopwords;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class assigns a boolean value to tokens that are in a specified stopword file.
 */
public class StopwordsList implements Stopwords {
    private static final Logger LOGGER = LogManager.getLogger(StopwordsList.class);

    private final Set<String> stopwordsList;

    public StopwordsList(Set<String> stopwordsList) {
        this.stopwordsList = Collections.unmodifiableSet(Objects.requireNonNull(stopwordsList));
    }

    public static StopwordsList loadFromInputStream(InputStream inputStream) throws IOException {
        LOGGER.info("Building stopwords list from input stream");
        Set<String> stopwordsListBuilder = new HashSet<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine;
        while ((nextLine = bufferedReader.readLine()) != null) {
            if (!nextLine.isEmpty()) {
                stopwordsListBuilder.add(nextLine.toLowerCase());
            }
        }

        return new StopwordsList(stopwordsListBuilder);
    }

    @Override
    public void checkIfStopword(Token token) {
        String value = token.getText().toLowerCase().trim();
        token.setIsStopword(stopwordsList.contains(value));
    }

    @Override
    public void annotateStopwords(Document document) {
        for (Token token : document.getTokens()) {
            checkIfStopword(token);
        }
    }
}
