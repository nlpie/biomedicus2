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
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuleBasedCandidateDetector implements DocumentProcessor {
    private final List<? extends KindCandidateDetector> candidateDetectors = Arrays.asList(new DrugKindCandidateDetector(),
            new AlcoholKindCandidateDetector(), new TobaccoKindCandidateDetector());
    private final Labels<SectionTitle> sectionTitleLabels;
    private final Labels<TermToken> termTokenLabels;
    private final Labels<SectionContent> sectionContentLabels;
    private final Labels<Sentence> sentenceLabels;
    private final Labels<Section> sectionLabels;
    private final Labeler<SocialHistoryCandidate> candidateLabeler;

    @Inject
    public RuleBasedCandidateDetector(Document document) {
        sectionLabels = document.labels(Section.class);
        sectionTitleLabels = document.labels(SectionTitle.class);
        termTokenLabels = document.labels(TermToken.class);
        candidateLabeler = document.labeler(SocialHistoryCandidate.class);
        sectionContentLabels = document.labels(SectionContent.class);
        sentenceLabels = document.labels(Sentence.class);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Section> sectionLabel : sectionLabels) {
            Label<SectionTitle> sectionTitleLabel = sectionTitleLabels.insideSpan(sectionLabel)
                    .firstOptionally()
                    .orElseThrow(() -> new BiomedicusException("Section did not have a title"));

            List<TermToken> titleTokens = termTokenLabels.insideSpan(sectionTitleLabel).values();
            List<KindCandidateDetector> headerMatches = new ArrayList<>();
            for (KindCandidateDetector candidateDetector : candidateDetectors) {
                if (candidateDetector.isSocialHistoryHeader(titleTokens)) {
                    headerMatches.add(candidateDetector);
                }
            }

            if (headerMatches.isEmpty()) {
                continue;
            }

            Label<SectionContent> sectionContentLabel = sectionContentLabels.insideSpan(sectionLabel).firstOptionally()
                    .orElseThrow(() -> new BiomedicusException("No section content"));
            Labels<Sentence> sentenceLabels = this.sentenceLabels.insideSpan(sectionContentLabel);
            for (Label<Sentence> sentenceLabel : sentenceLabels) {
                List<TermToken> sentenceTermTokens = termTokenLabels.insideSpan(sentenceLabel).values();
                for (KindCandidateDetector headerMatch : headerMatches) {
                    if (headerMatch.isSocialHistorySentence(titleTokens, sentenceTermTokens)) {
                        SubstanceUsageKind socialHistoryKind = headerMatch.getSocialHistoryKind();
                        SocialHistoryCandidate labelValue = new SocialHistoryCandidate(socialHistoryKind);
                        candidateLabeler.value(labelValue).label(sentenceLabel);
                    }
                }
            }
        }
    }
}
