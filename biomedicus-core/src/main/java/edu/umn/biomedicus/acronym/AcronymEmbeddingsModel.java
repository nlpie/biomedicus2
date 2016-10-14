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

package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An implementation of an acronym model that uses word vectors and a cosine distance metric
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@ProvidedBy(AcronymEmbeddingsModel.Loader.class)
class AcronymEmbeddingsModel implements AcronymModel {

    private final Embeddings embeddings;

    private final static int winHalf = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymEmbeddingsModel.class);

    private final AcronymExpansionsModel acronymExpansionsModel;

    AcronymEmbeddingsModel(Embeddings embeddings, AcronymExpansionsModel acronymExpansionsModel) {
        this.embeddings = embeddings;
        this.acronymExpansionsModel = acronymExpansionsModel;
    }

    /**
     * Will return a list of the possible senses for this acronym
     *
     * @param token a Token
     * @return a List of Strings of all possible senses
     */
    public Collection<String> getExpansions(Token token) {
        String acronym = Acronyms.standardAcronymForm(token);
        Collection<String> expansions = acronymExpansionsModel.getExpansions(acronym);
        if (expansions != null) {
            return expansions;
        }
        return Collections.emptyList();
    }

    /**
     * Does the model know about this acronym?
     * @param token a token
     * @return true if this token's text is a known acronym
     */
    public boolean hasAcronym(Token token) {
        String acronym = Acronyms.standardAcronymForm(token);
        return acronymExpansionsModel.hasExpansions(acronym);
    }

    /**
     * Will return the model's best guess for the sense of this acronym
     *
     * @param context a list of tokens including the full context for this acronym
     * @param forThisIndex an integer specifying the index of the acronym
     * @return
     */
    @Override
    public String findBestSense(List<Token> context, int forThisIndex) {

        String acronym = Acronyms.standardAcronymForm(context.get(forThisIndex));

        // If the model doesn't contain this acronym, make sure it doesn't contain an upper-case version of it
        Collection<String> casedSenses = acronymExpansionsModel.getExpansions(acronym);
        final Map<String, String> lowerToCasedSenses = new HashMap<>();
        casedSenses.forEach(s -> lowerToCasedSenses.put(s.toLowerCase(), s));
        Collection<String> senses = lowerToCasedSenses.keySet();
        if (senses == null) {
            senses = acronymExpansionsModel.getExpansions(acronym.toUpperCase());
        }
        if (senses == null) {
            senses = acronymExpansionsModel.getExpansions(acronym.replace(".", ""));
        }
        if (senses == null) {
            senses = acronymExpansionsModel.getExpansions(acronym.toLowerCase());
        }
        if (senses == null || senses.size() == 0) {
            return Acronyms.UNKNOWN;
        }

        // If the acronym is unambiguous, our work is done
        if (senses.size() == 1) {
            return senses.iterator().next();
        }

        List<String> usableSenses = new ArrayList<>();
        // Be sure that there even are disambiguation vectors for senses
        for (String sense : senses) {
            if (embeddings.contains(sense)) {
                usableSenses.add(sense);
            }
        }

        // If no senses good for disambiguation were found, try the upper-case version
        if (usableSenses.size() == 0 && acronymExpansionsModel.hasExpansions(acronym.toUpperCase())) {
            for (String sense : senses) {
                if (embeddings.contains(sense)) {
                    usableSenses.add(sense);
                }
            }
        }

        // Should this just guess the first sense instead?
        if (usableSenses.size() == 0) {
            return Acronyms.UNKNOWN;
        }

        double best = -Double.MAX_VALUE;
        String winner = Acronyms.UNKNOWN;

        WordEmbedding contextEmbedding = new WordEmbedding(embeddings.dimensionality());
        int leftEdge = Math.max(forThisIndex - winHalf, 0);
        int rightEdge = Math.min(forThisIndex + winHalf, context.size());
        for (int i=leftEdge; i<rightEdge; i++) {
            String word = context.get(i).text();
            if (embeddings.contains(word)) {
                contextEmbedding.add(embeddings.get(word));
            }
        }

        // Loop through all possible senses for this acronym
        for (String sense : usableSenses) {
            WordEmbedding compVec = embeddings.get(sense);
            double score = contextEmbedding.cosSim(compVec);
            if (score > best) {
                best = score;
                winner = lowerToCasedSenses.get(sense);
            }
        }
        return winner;
    }

    /**
     * Remove a single word from the model
     *
     * @param word the word to remove
     */
    public void removeWord(String word) {
        // todo
    }

    /**
     * Remove all words from the model except those given
     *
     * @param wordsToRemove the set of words to keep
     */
    public void removeWordsExcept(Set<String> wordsToRemove) {
        // todo
    }

    /**
     *
     */
    @Singleton
    static class Loader extends DataLoader<AcronymEmbeddingsModel> {
        private final Path embeddingsPath;

        private final AcronymExpansionsModel expansionsModel;

        @Inject
        public Loader(@Setting("acronym.embeddings.model.path") Path embeddingsPath,
                      AcronymExpansionsModel expansionsModel) {
            this.expansionsModel = expansionsModel;
            this.embeddingsPath = embeddingsPath;
        }

        @Override
        protected AcronymEmbeddingsModel loadModel() throws BiomedicusException {

            try {
                LOGGER.info("Loading embeddings: {}", embeddingsPath);
                @SuppressWarnings("unchecked")
                Embeddings embeddings = Embeddings.readBinFile(embeddingsPath.toString(), 0);
                embeddings.normalizeAll();
                // pre-weight all embeddings by their frequency rank
                for (String word : embeddings) {
                    embeddings.get(word).scalarMultiply(Math.log(1+embeddings.getRank(word)));
                }

                return new AcronymEmbeddingsModel(embeddings, expansionsModel);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
