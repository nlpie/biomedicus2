/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus

import edu.umn.biomedicus.acronyms.AcronymModule
import edu.umn.biomedicus.concepts.ConceptModule
import edu.umn.biomedicus.family.FamilyModule
import edu.umn.biomedicus.formatting.FormattingModule
import edu.umn.biomedicus.measures.MeasuresModule
import edu.umn.biomedicus.modification.ModifiersModule
import edu.umn.biomedicus.normalization.NormalizationModule
import edu.umn.biomedicus.parsing.ParsingModule
import edu.umn.biomedicus.sections.SectionsModule
import edu.umn.biomedicus.sentences.SentencesModule
import edu.umn.biomedicus.sh.SocialHistoryModule
import edu.umn.biomedicus.stopwords.StopwordsModule
import edu.umn.biomedicus.structure.StructureModule
import edu.umn.biomedicus.tagging.TaggingModule
import edu.umn.biomedicus.time.TimeModule
import edu.umn.biomedicus.tokenization.TokenizationModule
import edu.umn.nlpengine.System

class BiomedicusSystem : System() {
    override fun setup() {
        addModule(AcronymModule())
        addModule(ConceptModule())
        addModule(FamilyModule())
        addModule(FormattingModule())
        addModule(MeasuresModule())
        addModule(ModifiersModule())
        addModule(NormalizationModule())
        addModule(ParsingModule())
        addModule(SectionsModule())
        addModule(SentencesModule())
        addModule(SocialHistoryModule())
        addModule(StopwordsModule())
        addModule(StructureModule())
        addModule(TaggingModule())
        addModule(TimeModule())
        addModule(TokenizationModule())
    }
}