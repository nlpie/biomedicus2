/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.concepts;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Stores UMLS Concepts in a multimap (Map from String to List of Concepts).
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
@ProvidedBy(ConceptModelLoader.class)
class ConceptModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<CUI, List<TUI>> cuiToTUIs;

    private final Map<TermVector, List<CUI>> normDictionary;

    private final Map<String, SUI> phraseDictionary;

    private final Map<SUI, List<CUI>> suiToCUIs;

    public ConceptModel(Map<CUI, List<TUI>> cuiToTUIs, Map<TermVector, List<CUI>> normDictionary, Map<String, SUI> phraseDictionary, Map<SUI, List<CUI>> suiToCUIs) {
        this.cuiToTUIs = cuiToTUIs;
        this.normDictionary = normDictionary;
        this.phraseDictionary = phraseDictionary;
        this.suiToCUIs = suiToCUIs;
    }

    @Nullable
    SUI forPhrase(String phrase) {
        return phraseDictionary.get(phrase);
    }

    @Nullable
    List<CUI> forNorms(TermVector norms) {
        return normDictionary.get(norms);
    }

    @Nullable
    List<CUI> forSUI(SUI sui) {
        return suiToCUIs.get(sui);
    }

    @Nullable
    List<TUI> termsForCUI(CUI cui) {
        return cuiToTUIs.get(cui);
    }
}
