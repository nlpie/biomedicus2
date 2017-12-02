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

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.sh.SocialHistoryCandidate;

public interface KindSubstanceUsageDetector {

  String strTobaccoKeywords = "smoke|tobacco|ciga|pack|smoking|smokes";
  String strDrugKeywords = "drug|drugs|cocaine|illicit|illicits|substance|drugs|marijuana|benzodiazepines|cocaine|LSD|methamphetamine|heroin|opiates|recreational drugs|illicit drugs|narcotics|cocaine|crack|hashish";
  String strAlcoholKeywords = "ethanol|drink|drinks|drinking|alcohol|beer|glass|bottle|pint|drank|etoh|sobriety|sober";
  String strDayNames = "monday|mon|tuesday|tue|tu|tues|wednsday|wed|thursday|thur|th|thu|thurs|friday|fri|saturday|sat|sunday|sun";
  String strMonNames = "jan|january|feb|february|march|mar|april|apr|may|june|jun|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december";

  void processCandidate(TextView document, SocialHistoryCandidate socialHistoryCandidateLabel) throws BiomedicusException;
}
