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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.LifecycleManaged;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelsUtilities;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.tokenization.PennLikePhraseTokenizer;
import edu.umn.biomedicus.tokenization.TermTokenMerger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Vocabulary implements LifecycleManaged {
    private final Path dbPath;
    private boolean isOpen = false;
    @Nullable private DB db;
    @Nullable private MapDbTermIndex wordsIndex;
    @Nullable private MapDbTermIndex termsIndex;
    @Nullable private MapDbTermIndex normsIndex;

    @Inject
    public Vocabulary(@Setting("dictionary.db.path") Path dbPath) {
        this.dbPath = dbPath;
    }

    public TermIndex getWordsIndex() {
        checkOpen();
        return wordsIndex;
    }

    public TermIndex getTermsIndex() {
        checkOpen();
        return termsIndex;
    }

    public TermIndex getNormsIndex() {
        checkOpen();
        return normsIndex;
    }

    void addPhrase(String phrase) {
        checkOpen();
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
                wordsIndex.addTerm(term);
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
            termsIndex.addTerm(termToken.value().text());
        }
    }

    void addNormPhrase(String phrase) {
        checkOpen();
        Iterator<Span> normsIt = PennLikePhraseTokenizer.tokenizePhrase(phrase).iterator();
        while (normsIt.hasNext()) {
            Span span = normsIt.next();
            CharSequence norm = span.getCovered(phrase);
            normsIndex.addTerm(norm);
        }
    }

    private void checkOpen() {
        if (!isOpen) {
            throw new IllegalStateException("Vocabulary DB is not open.");
        }
    }

    private void openIndexes() {
        wordsIndex = new MapDbTermIndex(db, "words");
        termsIndex = new MapDbTermIndex(db, "terms");
        normsIndex = new MapDbTermIndex(db, "norms");
        isOpen = true;
    }

    void openForWriting() {
        db = DBMaker.fileDB(dbPath.toFile()).fileMmapEnableIfSupported().make();
        openIndexes();
    }
    @Override
    public void doStartup() throws BiomedicusException {
        db = DBMaker.fileDB(dbPath.toFile()).fileMmapEnableIfSupported().readOnly().make();
        openIndexes();
    }

    @Override
    public void doShutdown() throws BiomedicusException {
        if (wordsIndex != null) {
            wordsIndex.close();
            wordsIndex = null;
        }
        if (termsIndex != null) {
            termsIndex.close();
            termsIndex = null;
        }
        if (normsIndex != null) {
            normsIndex.close();
            normsIndex = null;
        }
        if (db != null) {
            db.close();
        }
        isOpen = false;
    }
}
