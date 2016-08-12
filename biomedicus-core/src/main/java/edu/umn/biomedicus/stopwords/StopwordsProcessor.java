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

package edu.umn.biomedicus.stopwords;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.semantics.StopWord;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.exc.BiomedicusException;

public class StopwordsProcessor implements DocumentProcessor {
    private final Stopwords stopwords;
    private final Labels<ParseToken> parseTokenLabels;
    private final ValueLabeler stopWordsLabeler;

    @Inject
    public StopwordsProcessor(Stopwords stopwords, Labels<ParseToken> parseTokenLabels, ValueLabeler stopWordsLabeler) {
        this.stopwords = stopwords;
        this.parseTokenLabels = parseTokenLabels;
        this.stopWordsLabeler = stopWordsLabeler;
    }

    @Inject
    public StopwordsProcessor(Stopwords stopwords,
                              Labels<ParseToken> parseTokenLabels,
                              Labeler<StopWord> stopWordLabeler) {
        this(stopwords, parseTokenLabels, stopWordLabeler.value(new StopWord()));
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<ParseToken> parseTokenLabel : parseTokenLabels) {
            if (stopwords.isStopWord(parseTokenLabel.value())) {
                stopWordsLabeler.label(parseTokenLabel);
            }
        }
    }
}
