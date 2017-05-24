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
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.LabelsUtilities;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class TermTokenMergerProcessor implements DocumentProcessor {
    private final LabelIndex<ParseToken> parseTokens;
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final Labeler<TermToken> termTokenLabeler;

    @Inject
    public TermTokenMergerProcessor(TextView document) {
        parseTokens = document.getLabelIndex(ParseToken.class);
        sentenceLabelIndex = document.getLabelIndex(Sentence.class);
        termTokenLabeler = document.getLabeler(TermToken.class);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
            LabelIndex<ParseToken> labelIndex = parseTokens.insideSpan(sentenceLabel);
            TermTokenMerger tokenMerger = new TermTokenMerger(LabelsUtilities.cast(labelIndex));
            while (tokenMerger.hasNext()) {
                Label<TermToken> termTokenLabel = tokenMerger.next();
                termTokenLabeler.label(termTokenLabel);
            }
        }
    }
}
