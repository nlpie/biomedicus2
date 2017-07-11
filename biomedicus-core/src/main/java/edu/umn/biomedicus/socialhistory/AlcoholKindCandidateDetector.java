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

public class AlcoholKindCandidateDetector implements KindCandidateDetector {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlcoholKindCandidateDetector.class);
  private Helpers helper = new Helpers();

  @Override
  public boolean isSocialHistoryHeader(List<ParseToken> headerTokens) {

    String strHeaderList = "(history|complaint|habits|subjectiv|allerg|risk +factor|hpi)";

    String strHeader = helper.toTokensString(headerTokens);
    if (strHeader.matches("(?is).*\\b" + strHeaderList + "\\b.*")) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isSocialHistorySentence(List<ParseToken> sectionTitleTokens,
      List<ParseToken> sentenceTokens) {

    String strRelative = "(mom|mother|dad|father|grandmother|grandfather|son|child|daughter|sister|brother|maternal|paternal|niece|nephew|cousin|grandchild)";
    String strAlcohol = "(alcoho|glass|alcohol|drinks|alcoholic|beer|bottle|cold turkey|drank|drink|beers|drinker|ETOH|Ex-drinker|Gin and Tonic|liquor|malt beverage|non-drinker|pint|rum|sober|spirit|whiskey|wine|ethanol|nondrinker|nonalcoholic)";

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
    if (!strSentence.matches("(?is).*" + strAlcohol + ".*")) {
      return false;
    }

    String strAlcoholType = "(alcohol|ethonal|alcoho|alcoholic beverage|beer|alcohol-containing |wine|Gin|Tonic|malt beverage|rum|wine|whiskey|ethanol|liquor|scotch|beers|vodka|etoh|wine|ETOH)";
    String StrNonAlcoholDrink = "(water|fluid|tea|coffee|solution|beverage|liquid|milk)";

    //# 1. has keywords other than drink ,drank, but not tea, coffee AND header in header list
    if (!strSentence.matches("(?is).*\\b(drink|drank|glass|bottle|drinks)\\b.*")
        && !strSentence.matches(
        "(?is).*\\b(alcohol-|blood +alcohol|good +spirit|Alcoholic +Dementia|eating +or +drinking|cirrhosis|benzyl +Alcohol|alcohol +intake|Alcohol/Drug|EtOH +withdrawal|alcoholic +hepatitis)\\b.*")
        ) {
      return true;
    }

    //2. Has drink word, but no water, fluid, tea, coffee, solution, beverage,liquid, eat AND header in header list
    if (strSentence.matches("(?is).*\\b(drink|drank|drinks)\\b.*")
        && !strSentence.matches("(?is).*\\b" + StrNonAlcoholDrink + "\\b.*")
        ) {
      return true;
    }

    //3. Has bottle, glass and alcohol type
    if (strSentence.matches("(?is).*\\b(bottle|glass)\\b.*")
        && !strSentence.matches("(?is).*\\b" + StrNonAlcoholDrink + "\\b.*")
        ) {
      return true;
    }

    if (strSentence.matches("(?is).*\\b" + strAlcoholType + "\\b.*")) {
      return true;
    }

        /*
         4. Has obvious pattern of discription of alcohol use
             1. history of alcohol abuse
             2. history of alcohol dependency
                BUT header NOT in header list
        */
    if (strSentence.matches("(?is).*\\bhistory +of +alcohol +(abuse|depend)\\b.*")
        || strSentence.matches("(?is).*\\balcohol abuse\\b.*")
        ) {
      return true;
    }

    return false;
  }

  @Override
  public SubstanceUsageKind getSocialHistoryKind() {
    return SubstanceUsageKind.ALCOHOL;
  }
}
