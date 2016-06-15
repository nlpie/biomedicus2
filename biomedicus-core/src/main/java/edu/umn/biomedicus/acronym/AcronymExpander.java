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

package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Will normalize/expand/disambiguate any acronyms or abbreviations that have been tagged by AcronymDetector.
 * Needs an AcronymModel to do this
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@DocumentScoped
class AcronymExpander implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymModel.class);
    private final AcronymModel model;
    private final Labeler<AcronymExpansion> acronymExpansionLabeler;
    private final Labels<Acronym> acronymLabels;
    private final Labels<TermToken> termTokenLabels;

    @Inject
    public AcronymExpander(@Setting("acronym.model") AcronymModel model,
                           Labels<TermToken> termTokenLabels,
                           Labels<Acronym> acronymLabels,
                           Labeler<AcronymExpansion> acronymExpansionLabeler) {
        this.model = model;
        this.termTokenLabels = termTokenLabels;
        this.acronymLabels = acronymLabels;
        this.acronymExpansionLabeler = acronymExpansionLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Expanding acronyms and abbreviations");

        List<TokenLike> allTokens = termTokenLabels.stream().map(Label::value).collect(Collectors.toList());

        for (Label<Acronym> acronymLabel : acronymLabels) {
            Label<TermToken> termTokenLabel = termTokenLabels.withSpan(acronymLabel).get();
            TermToken termToken = termTokenLabel.value();
            String sense = model.findBestSense(allTokens, termToken);
            if (!Acronyms.UNKNOWN.equals(sense) && !sense.equalsIgnoreCase(termToken.getText())) {
                acronymExpansionLabeler.value(new AcronymExpansion(sense, termToken.getTrailingText()))
                        .label(acronymLabel);
            }
        }

    }
}
