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
import edu.umn.biomedicus.common.types.semantics.ImmutableSocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Section;
import edu.umn.biomedicus.common.types.text.SectionContent;
import edu.umn.biomedicus.common.types.text.SectionTitle;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
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
    LabelIndex<Section> sectionLabels = systemView.getLabelIndex(Section.class);
    LabelIndex<SectionTitle> sectionTitleLabels = systemView.getLabelIndex(SectionTitle.class);
    LabelIndex<ParseToken> parseTokenLabels = systemView.getLabelIndex(ParseToken.class);
    Labeler<SocialHistoryCandidate> candidateLabeler = systemView
        .getLabeler(SocialHistoryCandidate.class);
    LabelIndex<SectionContent> sectionContentLabels = systemView
        .getLabelIndex(SectionContent.class);
    LabelIndex<Sentence> sentenceLabels = systemView.getLabelIndex(Sentence.class);

    for (Label<Section> sectionLabel : sectionLabels) {
      Label<SectionTitle> sectionTitleLabel = sectionTitleLabels
          .insideSpan(sectionLabel)
          .first()
          .orElseThrow(() -> new BiomedicusException(
              "Section did not have a title"));

      List<ParseToken> titleParseTokens = parseTokenLabels
          .insideSpan(sectionTitleLabel).valuesAsList();

      List<KindCandidateDetector> headerMatches = new ArrayList<>();
      for (KindCandidateDetector candidateDetector : candidateDetectors) {
        if (candidateDetector.isSocialHistoryHeader(titleParseTokens)) {
          headerMatches.add(candidateDetector);
        }
      }

      if (!headerMatches.isEmpty()) {

        Label<SectionContent> sectionContentLabel = sectionContentLabels
            .insideSpan(sectionLabel).first()
            .orElseThrow(() -> new BiomedicusException(
                "No section content"));
        LabelIndex<Sentence> sectionSentences = sentenceLabels.insideSpan(sectionContentLabel);

        for (Label<Sentence> sentenceLabel : sectionSentences) {

          List<ParseToken> sentenceParseTokens = parseTokenLabels
              .insideSpan(sentenceLabel).valuesAsList();
          for (KindCandidateDetector headerMatch : headerMatches) {

            if (headerMatch
                .isSocialHistorySentence(titleParseTokens,
                    sentenceParseTokens)) {

              SubstanceUsageKind socialHistoryKind = headerMatch
                  .getSocialHistoryKind();
              SocialHistoryCandidate labelValue
                  = ImmutableSocialHistoryCandidate
                  .builder()
                  .substanceUsageKind(socialHistoryKind)
                  .build();
              candidateLabeler.value(labelValue)
                  .label(sentenceLabel);

            }


          }
        }
      }


    }
  }
}
