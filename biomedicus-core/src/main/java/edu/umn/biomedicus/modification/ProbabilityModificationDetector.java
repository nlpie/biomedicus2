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
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.semantics.Probable;
import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class ProbabilityModificationDetector implements DocumentProcessor {
    private final ProbabilityModificationModel probabilityModificationModel;
    private final Document document;
    private final Labels<Sentence> sentences;
    private final Labels<DictionaryTerm> dictionaryTerms;
    private final Labels<TermToken> termTokens;
    private final Labels<PartOfSpeech> partsOfSpeech;
    private final ValueLabeler labeler;

    @Inject
    public ProbabilityModificationDetector(ProbabilityModificationModel probabilityModificationModel,
                                           Document document,
                                           Labels<Sentence> sentences,
                                           Labels<DictionaryTerm> dictionaryTerms,
                                           Labels<TermToken> termTokens,
                                           Labels<PartOfSpeech> partsOfSpeech,
                                           Labeler<Probable> labeler) {
        this.probabilityModificationModel = probabilityModificationModel;
        this.document = document;
        this.sentences = sentences;
        this.dictionaryTerms = dictionaryTerms;
        this.termTokens = termTokens;
        this.partsOfSpeech = partsOfSpeech;
        this.labeler = labeler.value(new Probable());
    }

    @Override
    public void process() throws BiomedicusException {
    ContextSearchBuilder contextSearchBuilder = new ContextSearchBuilder();
        contextSearchBuilder.setContextCues(probabilityModificationModel.getContextCues())
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
