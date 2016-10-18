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

package edu.umn.biomedicus.socialhistory;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuleBasedCandidateDetector implements DocumentProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TobaccoKindCandidateDetector.class);

    private final List<? extends KindCandidateDetector> candidateDetectors = Arrays.asList(new DrugKindCandidateDetector(),
            new AlcoholKindCandidateDetector(), new TobaccoKindCandidateDetector());
    private final LabelIndex<SectionTitle> sectionTitleLabels;
    private final LabelIndex<ParseToken> parseTokenLabels;
    private final LabelIndex<SectionContent> sectionContentLabels;
    private final LabelIndex<Sentence> sentenceLabels;
    private final LabelIndex<Section> sectionLabels;
    private final Labeler<SocialHistoryCandidate> candidateLabeler;

    @Inject
    public RuleBasedCandidateDetector(Document document) {

        sectionLabels = document.getLabelIndex(Section.class);
        sectionTitleLabels = document.getLabelIndex(SectionTitle.class);
        parseTokenLabels = document.getLabelIndex(ParseToken.class);
        candidateLabeler = document.getLabeler(SocialHistoryCandidate.class);
        sectionContentLabels = document.getLabelIndex(SectionContent.class);
        sentenceLabels = document.getLabelIndex(Sentence.class);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Section> sectionLabel : sectionLabelIndex) {
            Label<SectionTitle> sectionTitleLabel = sectionTitleLabelIndex.insideSpan(sectionLabel)
                    .first()
                    .orElseThrow(() -> new BiomedicusException("Section did not have a title"));

            List<ParseToken> titleParseTokens = parseTokenLabels.insideSpan(sectionTitleLabel).values();

            List<KindCandidateDetector> headerMatches = new ArrayList<>();
            for (KindCandidateDetector candidateDetector : candidateDetectors) {
                if (candidateDetector.isSocialHistoryHeader(titleParseTokens)) {
                    headerMatches.add(candidateDetector);
                }
            }

            if (!headerMatches.isEmpty()) {

                Label<SectionContent> sectionContentLabel = sectionContentLabels.insideSpan(sectionLabel).firstOptionally()
                        .orElseThrow(() -> new BiomedicusException("No section content"));
                LabelIndex<Sentence> sentenceLabels = this.sentenceLabels.insideSpan(sectionContentLabel);

                for (Label<Sentence> sentenceLabel : sentenceLabels) {

                    List<ParseToken> sentenceParseTokens = parseTokenLabels.insideSpan(sentenceLabel).values();
                    String strSentence = Helpers.toTokensString(sentenceParseTokens);
                    for (KindCandidateDetector headerMatch : headerMatches) {

                        if (headerMatch.isSocialHistorySentence(titleParseTokens, sentenceParseTokens)) {

                            SubstanceUsageKind socialHistoryKind = headerMatch.getSocialHistoryKind();
                            SocialHistoryCandidate labelValue = new SocialHistoryCandidate(socialHistoryKind);
                            candidateLabeler.value(labelValue).label(sentenceLabel);

                        }


                    }
                }
            }


        }
    }
}
