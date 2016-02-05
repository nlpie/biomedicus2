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

package edu.umn.biomedicus.concepts;

import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.model.semantics.Concept;
import edu.umn.biomedicus.model.simple.SimpleTerm;
import edu.umn.biomedicus.model.text.Span;
import edu.umn.biomedicus.model.text.Term;
import edu.umn.biomedicus.model.tokensets.OrderedTokenSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stores UMLS Concepts in a multimap (Map from String to List of Concepts).
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
@Singleton
class ConceptModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, List<String>> dictionary;

    private final Map<String, List<String>> cuiToTuis;

    /**
     * Default constructor. Takes a dictionary of phrases to their associated concepts.
     *
     * @param dictionary a multimap from a phrase to associated concepts
     */
    ConceptModel(Map<String, List<String>> dictionary, Map<String, List<String>> cuiToTuis) {
        this.dictionary = Objects.requireNonNull(dictionary);
        this.cuiToTuis = Objects.requireNonNull(cuiToTuis);
    }

    @Inject
    ConceptModel(BiomedicusConfiguration biomedicusConfiguration) {
        Path dictionaryPath = biomedicusConfiguration.resolveDataFile("concepts.dictionary.path");
        Path typesPath = biomedicusConfiguration.resolveDataFile("concepts.types.path");

        try (InputStream dictionaryIS = Files.newInputStream(dictionaryPath);
             InputStream typesIS = Files.newInputStream(typesPath)) {
            Yaml yaml = new Yaml(new SafeConstructor());

            LOGGER.info("Loading concepts dictionary: {}", dictionaryPath);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> conceptDictionary = (Map<String, List<String>>) yaml.load(dictionaryIS);
            this.dictionary = Objects.requireNonNull(conceptDictionary);

            LOGGER.info("Loading concept types: {}", typesPath);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> cuiToTuis = (Map<String, List<String>>) yaml.load(typesIS);
            this.cuiToTuis = Objects.requireNonNull(cuiToTuis);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load model files", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Attempts to find a concept for the orderedTokenSet by first checking their text and then checking their
     * normalized text.
     *
     * @param orderedTokenSet the tokens spanning the potential term that we should look up
     * @return a newly initialized term
     */
    public Term findConcepts(OrderedTokenSet orderedTokenSet) {
        String tokensText = orderedTokenSet.getTokensText().trim().toLowerCase();
        List<String> cuis = dictionary.get(tokensText);
        if (cuis == null || cuis.isEmpty()) {
            String normalizedTokensText = orderedTokenSet.getNormalizedTokensText().trim().toLowerCase();
            cuis = dictionary.get(normalizedTokensText);
        }

        Term term = null;
        if (cuis != null && cuis.size() > 0) {
            Span span = orderedTokenSet.getSpan();
            int begin = span.getBegin();
            int end = span.getEnd();

            List<Concept> concepts = cuis.stream()
                    .flatMap(cui -> cuiToTuis.get(cui).stream().map(tui -> new UmlsConcept(cui, tui)))
                    .collect(Collectors.toList());

            term = new SimpleTerm(begin, end, concepts.get(0), concepts.subList(1, concepts.size()));
        }

        return term;
    }
}
