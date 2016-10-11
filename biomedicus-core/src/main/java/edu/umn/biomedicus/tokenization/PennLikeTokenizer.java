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
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.Iterator;

public final class PennLikeTokenizer implements DocumentProcessor {
    private final Document document;
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final Labeler<ParseToken> parseTokenLabeler;

    @Inject
    public PennLikeTokenizer(Document document) {
        this.document = document;
        sentenceLabelIndex = document.getLabelIndex(Sentence.class);
        parseTokenLabeler = document.getLabeler(ParseToken.class);
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Sentence> sentence : sentenceLabelIndex) {
            CharSequence text = sentence.getCovered(document.getText());

            Iterator<Span> iterator = PennLikePhraseTokenizer.tokenizeSentence(text).iterator();

            Span last = null;
            while (iterator.hasNext()) {
                Span current = iterator.next();
                if (current.length() == 0) {
                    continue;
                }
                if (last != null) {
                    String tokenText = text.subSequence(last.getBegin(), last.getEnd()).toString();
                    boolean hasSpaceAfter = last.getEnd() != current.getBegin();
                    ParseToken parseToken = new ParseToken(tokenText, hasSpaceAfter);
                    parseTokenLabeler.value(parseToken).label(sentence.derelativize(last));
                }
                last = current;
            }
            if (last != null) {
                Span derelativized = sentence.derelativize(last);
                String tokenText = text.subSequence(last.getBegin(), last.getEnd()).toString();
                parseTokenLabeler.value(new ParseToken(tokenText, false)).label(derelativized);
            }
        }
    }
}
