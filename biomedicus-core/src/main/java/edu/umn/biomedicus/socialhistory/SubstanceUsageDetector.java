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

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.HashMap;
import java.util.Map;

public class SubstanceUsageDetector implements DocumentProcessor {

  private final Map<SubstanceUsageKind, KindSubstanceUsageDetector> kindMap;

  @Inject
  public SubstanceUsageDetector(
      TobaccoKindSubstanceUsageDetector tobaccoDetector,
      AlcoholKindSubstanceUsageDetector alcoholDetector,
      DrugKindSubstanceUsageDetector drugDetector
  ) {
    kindMap = new HashMap<>();
    kindMap.put(SubstanceUsageKind.NICOTINE, tobaccoDetector);
    kindMap.put(SubstanceUsageKind.ALCOHOL, alcoholDetector);
    kindMap.put(SubstanceUsageKind.DRUG, drugDetector);
  }

  @Override
  public void process(Document document) throws BiomedicusException {

    TextView systemView = StandardViews.getSystemView(document);

    for (Label<SocialHistoryCandidate> socialHistoryCandidateLabel
        : systemView.getLabelIndex(SocialHistoryCandidate.class)) {

      SubstanceUsageKind substanceUsageKind = socialHistoryCandidateLabel
          .value().substanceUsageKind();
      kindMap.get(substanceUsageKind).processCandidate(systemView, socialHistoryCandidateLabel);
    }

  }
}
