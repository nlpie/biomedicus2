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

import edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind;
import edu.umn.biomedicus.common.types.text.ParseToken;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrugKindCandidateDetector implements KindCandidateDetector {

  private static final Logger LOGGER = LoggerFactory.getLogger(DrugKindCandidateDetector.class);
  private Helpers helper = new Helpers();

  @Override
  public boolean isSocialHistoryHeader(List<ParseToken> headerTokens) {

    String strHeaderList = "(history|complaint|habits|subjectiv|allerg|risk +factor|hpi|Present +illness|Current +level +of +activity|Present +illness|Cardiovascular +risk +analysis|drug)";
    String sttHeaderListExact = "(ESRD|SH|SHX)";

    String strHeader = helper.toTokensString(headerTokens);
    if (strHeader.matches("(?is).*\\b" + strHeaderList + "\\b.*") || strHeader
        .matches("(?s).*\\b" + sttHeaderListExact + "\\b.*")) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isSocialHistorySentence(List<ParseToken> sectionTitleTokens,
      List<ParseToken> sentenceTokens) {

    String strRelative = "(mom|mother|dad|father|grandmother|grandfather|son|child|daughter|sister|brother|maternal|paternal|niece|nephew|cousin|grandchild)";
    String strDrug = "(drug|snort|intranasal|marijuana|benzodiazepine|cocaine|LSD|methamphetamine|heroin|opiate|recreational drug|illicit drug|narcotic|hashish|crack |Other drug|unprescribed drug|injected drug|use drug|drug abuse|drug use|intravenous drug|illicit substance|drug user|polysubstance abuse|ETOH drug|on drug|illicit)";

    String strSentence = helper.toTokensString(sentenceTokens);
    String strHeader = helper.toTokensString(sectionTitleTokens);
    strHeader = "SOCIAL HISTORY";

    if (!strSentence.matches(".*[a-zA-Z].*")) {
      return false;
    }
    if (strSentence == strHeader) {
      return false;
    }
    if (strSentence.matches("(?s).*:$")) {
      return false;
    }
    if (strSentence.matches("(?is).*\\b" + strRelative + "\\b.*")) {
      return false;
    }
    if (!strSentence.matches("(?is).*" + strDrug + ".*")) {
      return false;
    }

    if (strSentence.matches("(?is).*\\bintranasal\\b.*") && strSentence
        .matches("(?is) .*\\bflu\\b.*")) {
      return false;
    }

    if (strSentence.matches("(?is).*\\bnarcotic\\b.*")) {
      return false;
    }

    if (strSentence.matches("(?is).*\\b(crack|cracked|cracks)\\b.*")) {
      return false;
    }

    if (strSentence.matches("(?is).*\\bdrug +use\\b.*") && strSentence
        .matches("(?is) .*\\binflammatory\\b.*")) {
      return false;
    }
    if (strSentence.matches("(?is).*\\bsnort arousal\\b.*")) {
      return false;
    }

    return true;


  }

  @Override
  public SubstanceUsageKind getSocialHistoryKind() {
    return SubstanceUsageKind.DRUG;
  }
}

