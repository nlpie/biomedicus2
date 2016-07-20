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
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.*;

public final class TermTokenMerger implements DocumentProcessor {
    private static final Set<Character> MERGE = new HashSet<>(Arrays.asList('-', '/', '\\', '\'', '_'));
    private final Labels<ParseToken> parseTokens;
    private final Labeler<TermToken> termTokenLabeler;
    private final Labels<Sentence> sentenceLabels;

    @Inject
    public TermTokenMerger(Labels<Sentence> sentenceLabels,
                           Labels<ParseToken> parseTokens,
                           Labeler<TermToken> termTokenLabeler) {
        this.parseTokens = parseTokens;
        this.termTokenLabeler = termTokenLabeler;
        this.sentenceLabels = sentenceLabels;
    }

    @Override
    public void process() throws BiomedicusException {
        List<Label<ParseToken>> running = new ArrayList<>();
        for (Label<Sentence> sentenceLabel : sentenceLabels) {
            for (Label<ParseToken> tokenLabel : parseTokens.insideSpan(sentenceLabel)) {
                if (running.size() == 0) {
                    running.add(tokenLabel);
                    continue;
                }

                Label<ParseToken> lastLabel = running.get(running.size() - 1);
                ParseToken lastToken = lastLabel.value();
                String lastText = lastToken.text();
                char last = lastText.charAt(lastText.length() - 1);
                String text = tokenLabel.value().text();
                char first = text.charAt(0);
                if (lastToken.hasSpaceAfter() || !shouldMerge(last, first)) {
                    makeTermToken(running);
                    running.clear();
                }
                running.add(tokenLabel);
            }
            makeTermToken(running);
            running.clear();
        }
    }

    private boolean shouldMerge(char last, char first) {
        if (MERGE.contains(first) && Character.isLetterOrDigit(last)) {
            return true;
        }
        if (Character.isLetterOrDigit(first) && MERGE.contains(last)) {
            return true;
        }
        return Character.isLetterOrDigit(first) && Character.isLetterOrDigit(last);
    }

    private void makeTermToken(List<Label<ParseToken>> running) throws BiomedicusException {
        if (running.size() == 0) {
            return;
        }
        StringBuilder tokenText = new StringBuilder();
        for (Label<ParseToken> label : running) {
            ParseToken token = label.value();
            tokenText.append(token.text());
        }
        Label<ParseToken> lastTokenLabel = running.get(running.size() - 1);
        boolean hasSpaceAfter = lastTokenLabel.value().hasSpaceAfter();

        TermToken termToken = new TermToken(tokenText.toString(), hasSpaceAfter);
        termTokenLabeler.value(termToken).label(new Span(running.get(0).getBegin(), lastTokenLabel.getEnd()));
    }
}
