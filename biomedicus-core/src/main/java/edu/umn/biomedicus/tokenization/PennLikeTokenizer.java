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
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.Iterator;

@DocumentScoped
public class PennLikeTokenizer implements DocumentProcessor {
    private final Document document;
    private final Labeler<ParseToken> parseTokenLabeler;

    @Inject
    public PennLikeTokenizer(Document document,
                             Labeler<ParseToken> parseTokenLabeler) {
        this.document = document;
        this.parseTokenLabeler = parseTokenLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            String text = sentence.getText();

            PennLikeSentenceTokenizer sentenceTokenizer = new PennLikeSentenceTokenizer(text);

            Iterator<PennLikeSentenceTokenizer.TokenCandidate> iterator = sentenceTokenizer.startStreamWithWords()
                    .flatMap(sentenceTokenizer::splitTrailingPeriod)
                    .flatMap(sentenceTokenizer::splitWordByMiddleBreaks)
                    .flatMap(sentenceTokenizer::splitWordByBeginBreaks)
                    .flatMap(sentenceTokenizer::splitWordByEndBreaks)
                    .iterator();

            Span last = null;
            while (iterator.hasNext()) {
                PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = iterator.next();
                Span span = tokenCandidate.toSpan();
                if (span.length() == 0) {
                    continue;
                }
                if (last != null) {
                    String tokenText = text.substring(last.getBegin(), last.getEnd());
                    String trailingText = text.substring(last.getEnd(), span.getBegin());
                    ParseToken parseToken = new ParseToken(tokenText, trailingText);
                    parseTokenLabeler.value(parseToken).label(sentence.derelativize(last));
                }
                last = span;
            }
            if (last != null) {
                Span derelativized = sentence.derelativize(last);
                parseTokenLabeler.value(new ParseToken(text.substring(last.getBegin(), last.getEnd()), ""))
                        .label(derelativized);
            }
        }
    }
}
