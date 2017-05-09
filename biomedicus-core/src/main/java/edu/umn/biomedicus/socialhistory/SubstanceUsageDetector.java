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
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.HashMap;
import java.util.Map;

public class SubstanceUsageDetector implements DocumentProcessor {

    private final LabelIndex<TermToken> termTokenLabels;
    private final TobaccoKindSubstanceUsageDetector
            tobaccoKindSubstanceUsageDetector;
    private final AlcoholKindSubstanceUsageDetector
            alcoholKindSubstanceUsageDetector;
    private final DrugKindSubstanceUsageDetector drugKindSubstanceUsageDetector;
    private final TextView document;
    private final Map<SubstanceUsageKind, KindSubstanceUsageDetector> kindMap;
    private Helpers helper = new Helpers();
    private LabelIndex<ParseToken> parseTokenLabels;

    @Inject
    public SubstanceUsageDetector(TextView document,
                                  TobaccoKindSubstanceUsageDetector tobaccoKindSubstanceUsageDetector,
                                  AlcoholKindSubstanceUsageDetector alcoholKindSubstanceUsageDetector,
                                  DrugKindSubstanceUsageDetector drugKindSubstanceUsageDetector) {

        termTokenLabels = document.getLabelIndex(TermToken.class);
        this.document = document;
        this.tobaccoKindSubstanceUsageDetector
                = tobaccoKindSubstanceUsageDetector;
        this.alcoholKindSubstanceUsageDetector
                = alcoholKindSubstanceUsageDetector;
        this.drugKindSubstanceUsageDetector = drugKindSubstanceUsageDetector;
        parseTokenLabels = document.getLabelIndex(ParseToken.class);
        kindMap = createKindMap();
    }


    private Map<SubstanceUsageKind, KindSubstanceUsageDetector> createKindMap() {

        Map<SubstanceUsageKind, KindSubstanceUsageDetector>
                kindSubstanceUsageDetectorMap = new HashMap<>();

        kindSubstanceUsageDetectorMap.put(SubstanceUsageKind.NICOTINE,
                tobaccoKindSubstanceUsageDetector);
        kindSubstanceUsageDetectorMap.put(SubstanceUsageKind.ALCOHOL,
                alcoholKindSubstanceUsageDetector);
        kindSubstanceUsageDetectorMap
                .put(SubstanceUsageKind.DRUG, drugKindSubstanceUsageDetector);


        return kindSubstanceUsageDetectorMap;
    }


    @Override
    public void process() throws BiomedicusException {

        for (Label<SocialHistoryCandidate> socialHistoryCandidateLabel
                : document.getLabelIndex(SocialHistoryCandidate.class)) {

            SubstanceUsageKind substanceUsageKind = socialHistoryCandidateLabel
                    .value().substanceUsageKind();
            kindMap.get(substanceUsageKind)
                    .processCandidate(document, socialHistoryCandidateLabel);
        }

    }
}
