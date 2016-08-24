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

import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;

final class ContextSearchBuilder {
    private ContextCues contextCues;
    private Document document;
    private Labels<Sentence> sentences;
    private Labels<?> modifiableTerms;
    private Labels<?> tokens;
    private Labels<PartOfSpeech> partOfSpeechLabels;

    ContextSearchBuilder setContextCues(ContextCues contextCues) {
        this.contextCues = contextCues;
        return this;
    }

    ContextSearchBuilder setDocument(Document document) {
        this.document = document;
        return this;
    }

    ContextSearchBuilder setSentences(Labels<Sentence> sentences) {
        this.sentences = sentences;
        return this;
    }

    ContextSearchBuilder setModifiableTerms(Labels<?> modifiableTerms) {
        this.modifiableTerms = modifiableTerms;
        return this;
    }

    ContextSearchBuilder setTokens(Labels<?> tokens) {
        this.tokens = tokens;
        return this;
    }

    ContextSearchBuilder setPartOfSpeechLabels(Labels<PartOfSpeech> partOfSpeechLabels) {
        this.partOfSpeechLabels = partOfSpeechLabels;
        return this;
    }

    ContextSearch createContextSearch() {
        return new ContextSearch(contextCues, document, sentences, modifiableTerms, tokens, partOfSpeechLabels);
    }
}