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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageElement;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.DependencyParse;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;

public class TobaccoKindSubstanceUsageDetector implements KindSubstanceUsageDetector {
    @Override
    public void processCandidate(Document document, Label<SocialHistoryCandidate> socialHistoryCandidateLabel) throws BiomedicusException {

        Label<Sentence> sentenceLabel = document.getLabelIndex(Sentence.class).withTextLocation(socialHistoryCandidateLabel)
                .orElseThrow(() -> new BiomedicusException("SocialHistory Candidate does not have sentence"));
        Label<DependencyParse> dependencyParseLabel = document.getLabelIndex(DependencyParse.class).withTextLocation(sentenceLabel)
                .orElseThrow(() -> new BiomedicusException("No parse for sentence."));

        SubstanceUsageElement value = new SubstanceUsageElement(SubstanceUsageElementType.AMOUNT, SubstanceUsageKind.NICOTINE);
        document.getLabeler(SubstanceUsageElement.class).value(value).label(sentenceLabel);
    }
}
