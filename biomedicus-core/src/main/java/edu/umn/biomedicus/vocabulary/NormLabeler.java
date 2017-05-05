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

package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.types.text.ImmutableNormIndex;
import edu.umn.biomedicus.common.types.text.NormForm;
import edu.umn.biomedicus.common.types.text.NormIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class NormLabeler implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormLabeler.class);
    private final TermIndex wordIndex;
    private final LabelIndex<NormForm> normFormLabelIndex;
    private final Labeler<NormIndex> normIndexLabeler;

    @Inject
    public NormLabeler(Vocabulary vocabulary, TextView document) {
        this.wordIndex = vocabulary.getNormsIndex();
        normFormLabelIndex = document.getLabelIndex(NormForm.class);
        normIndexLabeler = document.getLabeler(NormIndex.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Labeling norm term index identifiers in a document.");
        for (Label<NormForm> normFormLabel : normFormLabelIndex) {
            String normalForm = normFormLabel.value().normalForm();
            IndexedTerm term = wordIndex.getIndexedTerm(normalForm);
            NormIndex normIndex = ImmutableNormIndex.builder()
                    .term(term)
                    .build();
            normIndexLabeler.value(normIndex).label(normFormLabel);
        }
    }
}
