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
import edu.umn.biomedicus.acronym.Acronyms;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.collect.DistinctSpansMap;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.*;
import java.util.stream.Collectors;

@DocumentScoped
public class TermTokenMerger implements DocumentProcessor {
    private static final Set<Character> MERGE = new HashSet<>(Arrays.asList('-', '/', '\\', '\''));
    private final Labels<ParseToken> parseTokens;
    private final Labeler<TermToken> termTokenLabeler;

    @Inject
    public TermTokenMerger(Labels<ParseToken> parseTokens,
                           Labeler<TermToken> termTokenLabeler) {
        this.parseTokens = parseTokens;
        this.termTokenLabeler = termTokenLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        List<Label<ParseToken>> running = new ArrayList<>();
        for (Label<ParseToken> parseToken : parseTokens) {
            if (running.size() == 0) {
                running.add(parseToken);
                continue;
            }

            Label<ParseToken> lastLabel = running.get(running.size() - 1);
            ParseToken lastToken = lastLabel.value();
            String lastText = lastToken.getText();
            char last = lastText.charAt(lastText.length() - 1);
            String text = parseToken.value().getText();
            char first = text.charAt(0);
            boolean noTrailing = lastToken.getTrailingText().length() > 0;
            if (noTrailing || !shouldMerge(last, first)) {
                makeTermToken(running);
                running.clear();
            }
            running.add(parseToken);
        }
        makeTermToken(running);
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
        for (int i = 0; i < running.size() - 1; i++) {
            Label<ParseToken> label = running.get(i);
            ParseToken value = label.value();
            tokenText.append(value.getText());
            tokenText.append(value.getTrailingText());
        }
        Label<ParseToken> lastRunningLabel = running.get(running.size() - 1);
        tokenText.append(lastRunningLabel.value().getText());

        termTokenLabeler.value(new TermToken(tokenText.toString(), lastRunningLabel.value().getTrailingText()))
                .label(new Span(running.get(0).getBegin(), lastRunningLabel.getEnd()));
    }
}
