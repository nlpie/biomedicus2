/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.common.viterbi.ViterbiProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
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

  /**
   * Default constructor. Initializes the beam threshold and tnt model.
   *
   * @param tntModel tnt model.
   * @param beamThreshold beam threshold in log base 10. The difference from the most probable to
   * exclude.
   */
  @Inject
  public TntPosTagger(
      TntModel tntModel,
      @Setting("tnt.beam.threshold") Double beamThreshold
  ) {
    this.tntModel = tntModel;
    this.beamThreshold = beamThreshold;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    Labeler<PosTag> partOfSpeechLabeler = systemView.getLabeler(PosTag.class);

    for (Sentence sentence : sentenceLabelIndex) {
      Collection<ParseToken> tokens = parseTokenLabelIndex.insideSpan(sentence);
      ViterbiProcessor<PosCap, WordCap> viterbiProcessor = Viterbi.secondOrder(tntModel, tntModel,
          Ngram.create(BBS, BOS), Ngram::create);

      String docText = systemView.getText();
      for (ParseToken token : tokens) {
        CharSequence text = token.coveredText(docText);
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
      for (ParseToken token : tokens) {
        PartOfSpeech partOfSpeech = it.next().getPartOfSpeech();
        partOfSpeechLabeler.add(new PosTag(token, partOfSpeech));
      }
    }
  }
}
