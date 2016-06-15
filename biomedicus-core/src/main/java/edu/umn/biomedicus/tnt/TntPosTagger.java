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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.common.viterbi.ViterbiProcessor;

import java.util.List;

/**
 * Part of speech tagger implementation for the TnT algorithm.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class TntPosTagger {
    /**
     * A pos cap for before the beginning of sentences.
     */
    public static final PosCap BBS = PosCap.getNotCapitalized(PartOfSpeech.BBS);

    /**
     * The pos cap for the beginning of the sentence.
     */
    public static final PosCap BOS = PosCap.getNotCapitalized(PartOfSpeech.BOS);

    /**
     * The pos cap for skipping
     */
    public static final PosCap SKIP = PosCap.getNotCapitalized(PartOfSpeech.XX);

    /**
     * The pos cap for end of sentences.
     */
    public static final PosCap EOS = PosCap.getNotCapitalized(PartOfSpeech.EOS);

    /**
     * The beam threshold in log base 10. Difference from most probable to exclude.
     */
    private final double beamThreshold;

    /**
     * The tnt model to use.
     */
    private final TntModel tntModel;

    /**
     * Default constructor. Initializes the beam threshold and tnt model.
     *
     * @param tntModel      tnt model.
     * @param beamThreshold beam threshold in log base 10. The difference from the most probable to exclude.
     */
    public TntPosTagger(TntModel tntModel, double beamThreshold) {
        this.tntModel = tntModel;
        this.beamThreshold = beamThreshold;
    }

    public void tagSentence(Sentence sentence) {
        List<Token> tokens = sentence.getTokens();
        ViterbiProcessor<PosCap, WordCap> viterbiProcessor = Viterbi.secondOrder(tntModel, tntModel, Ngram.create(BBS, BOS),
                Ngram::create);

        for (Token token : tokens) {
            String text = token.getText();
            boolean isCapitalized = Character.isUpperCase(text.charAt(0));
            viterbiProcessor.advance(new WordCap(text, isCapitalized));
            viterbiProcessor.beamFilter(beamThreshold);
        }

        List<PosCap> tags = viterbiProcessor.end(SKIP, EOS);

        if (tokens.size() + 2 != tags.size()) {
            throw new AssertionError("Tags should be same size as number of tokens in sentence");
        }

        for (int i = 2; i < tags.size(); i++) {
            PartOfSpeech partOfSpeech = tags.get(i).getPartOfSpeech();
            Token token = tokens.get(i - 2);
            token.setPennPartOfSpeech(partOfSpeech);
        }
    }
}
