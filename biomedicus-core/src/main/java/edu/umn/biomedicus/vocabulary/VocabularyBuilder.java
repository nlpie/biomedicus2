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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelsUtilities;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.tokenization.PennLikePhraseTokenizer;
import edu.umn.biomedicus.tokenization.TermTokenMerger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VocabularyBuilder {
    private final MapDbTermIndex words;
    private final MapDbTermIndex norms;
    private final MapDbTermIndex terms;

    public VocabularyBuilder(Path dbPath) {
        DB db = DBMaker.fileDB(dbPath.toFile()).fileMmapEnableIfSupported().closeOnJvmShutdown().make();
        words = new WordsIndex(dbPath);
        words.openForWriting(db);
        norms = new NormsIndex(dbPath);
        norms.openForWriting(db);
        terms = new TermsIndex(dbPath);
        terms.openForWriting(db);
    }

    public void addPhrase(String phrase) {
        Iterator<Span> tokensIterator = PennLikePhraseTokenizer.tokenizePhrase(phrase).iterator();
        List<Label<Token>> parseTokens = new ArrayList<>();
        Span prev = null;
        while (tokensIterator.hasNext() || prev != null) {
            Span span = null;
            if (tokensIterator.hasNext()) {
                span = tokensIterator.next();
            }
            if (prev != null) {
                String term = prev.getCovered(phrase).toString();
                words.addTerm(term);
                boolean hasSpaceAfter = span != null && prev.getEnd() != span.getBegin();
                ParseToken parseToken = new ParseToken(term, hasSpaceAfter);
                Label<ParseToken> parseTokenLabel = new Label<>(prev, parseToken);
                parseTokens.add(LabelsUtilities.cast(parseTokenLabel));
            }
            prev = span;
        }

        TermTokenMerger termTokenMerger = new TermTokenMerger(parseTokens);
        while (termTokenMerger.hasNext()) {
            Label<TermToken> termToken = termTokenMerger.next();
            terms.addTerm(termToken.value().text());
        }
    }

    public void addNormPhrase(String phrase) {
        Iterator<Span> normsIt = PennLikePhraseTokenizer.tokenizePhrase(phrase).iterator();
        while (normsIt.hasNext()) {
            Span span = normsIt.next();
            CharSequence norm = span.getCovered(phrase);
            norms.addTerm(norm);
        }
    }
}
