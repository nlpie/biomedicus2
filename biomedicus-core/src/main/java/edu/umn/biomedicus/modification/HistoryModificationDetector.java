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

package edu.umn.biomedicus.modification;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.types.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.types.semantics.Historical;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class HistoryModificationDetector implements DocumentProcessor {
    private final HistoryModificationModel historyModificationModel;
    private final Document document;
    private final Labels<Sentence> sentences;
    private final Labels<DictionaryTerm> dictionaryTerms;
    private final Labels<TermToken> termTokens;
    private final Labels<PartOfSpeech> partsOfSpeech;
    private final ValueLabeler labeler;

    @Inject
    public HistoryModificationDetector(HistoryModificationModel historyModificationModel,
                                       Document document) {
        this.historyModificationModel = historyModificationModel;
        this.document = document;
        this.sentences = document.labels(Sentence.class);
        this.dictionaryTerms = document.labels(DictionaryTerm.class);
        this.termTokens = document.labels(TermToken.class);
        this.partsOfSpeech = document.labels(PartOfSpeech.class);
        labeler = document.labeler(Historical.class).value(new Historical());
    }

    @Override
    public void process() throws BiomedicusException {
        ContextSearchBuilder contextSearchBuilder = new ContextSearchBuilder();
        contextSearchBuilder.setContextCues(historyModificationModel.getContextCues())
                .setDocument(document)
                .setSentences(sentences)
                .setModifiableTerms(dictionaryTerms)
                .setTokens(termTokens)
                .setPartOfSpeechLabels(partsOfSpeech);
        ContextSearch contextSearch = contextSearchBuilder.createContextSearch();
        for (Span span : contextSearch.findMatches()) {
            labeler.label(span);
        }
    }
}
