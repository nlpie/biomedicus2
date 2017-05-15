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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.common.viterbi.ViterbiProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Part of speech tagger implementation for the TnT algorithm.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class TntPosTagger implements DocumentProcessor {
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
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final LabelIndex<ParseToken> parseTokenLabelIndex;
    private final TextView document;
    private final Labeler<PartOfSpeech> partOfSpeechLabeler;

    /**
     * Default constructor. Initializes the beam threshold and tnt model.
     *
     * @param tntModel      tnt model.
     * @param beamThreshold beam threshold in log base 10. The difference from the most probable to exclude.
     */
    @Inject
    public TntPosTagger(TntModel tntModel,
                        @Setting("tnt.beam.threshold") Double beamThreshold,
                        TextView document) {
        this.tntModel = tntModel;
        this.beamThreshold = beamThreshold;
        this.document = document;
        sentenceLabelIndex = document.getLabelIndex(Sentence.class);
        parseTokenLabelIndex = document.getLabelIndex(ParseToken.class);
        partOfSpeechLabeler = document.getLabeler(PartOfSpeech.class);
    }

    public void tagSentence(Label<Sentence> sentence2Label)
            throws BiomedicusException {
        Collection<Label<ParseToken>> tokens = parseTokenLabelIndex
                .insideSpan(sentence2Label);
        ViterbiProcessor<PosCap, WordCap> viterbiProcessor = Viterbi
                .secondOrder(tntModel, tntModel, Ngram.create(BBS, BOS),
                        Ngram::create);

        for (Label<ParseToken> token : tokens) {
            CharSequence text = token.getCovered(document.getText());
            boolean isCapitalized = Character.isUpperCase(text.charAt(0));
            viterbiProcessor
                    .advance(new WordCap(text.toString(), isCapitalized));
            viterbiProcessor.beamFilter(beamThreshold);
        }

        List<PosCap> tags = viterbiProcessor.end(SKIP, EOS);

        if (tokens.size() + 2 != tags.size()) {
            throw new AssertionError(
                    "Tags should be same size as number of tokens in sentence");
        }

        Iterator<PosCap> it = tags.subList(2, tags.size()).iterator();
        for (Label<ParseToken> token : tokens) {
            PartOfSpeech partOfSpeech = it.next().getPartOfSpeech();
            partOfSpeechLabeler.value(partOfSpeech).label(token);
        }
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Sentence> sentence2Label : sentenceLabelIndex) {
            tagSentence(sentence2Label);
        }
    }
}
