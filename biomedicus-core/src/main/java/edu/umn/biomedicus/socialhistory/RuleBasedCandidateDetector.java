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

package edu.umn.biomedicus.socialhistory;

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.sections.Section;
import edu.umn.biomedicus.sections.SectionContent;
import edu.umn.biomedicus.sections.SectionTitle;
import edu.umn.biomedicus.sections.SubstanceUsageKind;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.sh.SocialHistoryCandidate;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuleBasedCandidateDetector implements DocumentProcessor {

  private final List<? extends KindCandidateDetector> candidateDetectors = Arrays.asList(
      new DrugKindCandidateDetector(),
      new AlcoholKindCandidateDetector(),
      new TobaccoKindCandidateDetector()
  );

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);
    LabelIndex<Section> sections = systemView.getLabelIndex(Section.class);
    LabelIndex<SectionTitle> sectionTitles = systemView.getLabelIndex(SectionTitle.class);
    LabelIndex<ParseToken> parseTokenLabels = systemView.getLabelIndex(ParseToken.class);
    LabelIndex<SectionContent> sectionContents = systemView.getLabelIndex(SectionContent.class);
    LabelIndex<Sentence> sentences = systemView.getLabelIndex(Sentence.class);

    Labeler<SocialHistoryCandidate> candidateLabeler =
        systemView.getLabeler(SocialHistoryCandidate.class);

    for (Section section : sections) {
      SectionTitle sectionTitleLabel = sectionTitles.insideSpan(section)
          .first();
      List<ParseToken> titleParseTokens = parseTokenLabels.insideSpan(sectionTitleLabel).asList();

      List<KindCandidateDetector> headerMatches = new ArrayList<>();
      for (KindCandidateDetector candidateDetector : candidateDetectors) {
        if (candidateDetector.isSocialHistoryHeader(titleParseTokens)) {
          headerMatches.add(candidateDetector);
        }
      }

      if (!headerMatches.isEmpty()) {
        SectionContent sectionContent = sectionContents.insideSpan(section).first();
        LabelIndex<Sentence> sectionSentences = sentences.insideSpan(sectionContent);

        for (Sentence sentence : sectionSentences) {
          List<ParseToken> sentenceParseTokens = parseTokenLabels.insideSpan(sentence).asList();
          for (KindCandidateDetector headerMatch : headerMatches) {
            if (headerMatch.isSocialHistorySentence(titleParseTokens, sentenceParseTokens)) {
              SubstanceUsageKind socialHistoryKind = headerMatch.getSocialHistoryKind();
              candidateLabeler.add(new SocialHistoryCandidate(sentence, socialHistoryKind));
            }
          }
        }
      }
    }
  }
}
