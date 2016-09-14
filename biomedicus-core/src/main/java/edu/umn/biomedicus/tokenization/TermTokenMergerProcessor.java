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

package edu.umn.biomedicus.tokenization;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.LabelsUtilities;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class TermTokenMergerProcessor implements DocumentProcessor {
    private final Labels<ParseToken> parseTokens;
    private final Labels<Sentence> sentenceLabels;
    private final Labeler<TermToken> termTokenLabeler;

    @Inject
    public TermTokenMergerProcessor(Document document) {
        parseTokens = document.labels(ParseToken.class);
        sentenceLabels = document.labels(Sentence.class);
        termTokenLabeler = document.labeler(TermToken.class);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Sentence> sentenceLabel : sentenceLabels) {
            Labels<ParseToken> labels = parseTokens.insideSpan(sentenceLabel);
            TermTokenMerger tokenMerger = new TermTokenMerger(LabelsUtilities.cast(labels));
            while (tokenMerger.hasNext()) {
                Label<TermToken> termTokenLabel = tokenMerger.next();
                termTokenLabeler.label(termTokenLabel);
            }
        }
    }
}
