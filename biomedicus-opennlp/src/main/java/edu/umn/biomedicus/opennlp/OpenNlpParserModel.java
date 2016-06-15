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

package edu.umn.biomedicus.opennlp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
class OpenNlpParserModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNlpParserModel.class);

    private final ParserModel parserModel;

    @Inject
    OpenNlpParserModel(@Setting("opennlp.parser.model.path") Path path) throws BiomedicusException {
        LOGGER.info("Loading OpenNLP parser model: {}", path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            parserModel = new ParserModel(inputStream);
        } catch (IOException e) {
            throw new BiomedicusException("Failed to load OpenNLP parser model: {}", e);
        }
    }

    Parser createParser() {
        return new ParserNoTagging(parserModel);
    }
}
