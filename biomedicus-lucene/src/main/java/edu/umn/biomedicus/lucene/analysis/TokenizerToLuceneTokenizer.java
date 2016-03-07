/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.lucene.analysis;

import edu.umn.biomedicus.common.simple.SimpleDocument;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.processing.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 */
public class TokenizerToLuceneTokenizer extends org.apache.lucene.analysis.Tokenizer {
    private final Tokenizer tokenizer;

    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private Iterator<Token> tokenIterator;

    protected TokenizerToLuceneTokenizer(Tokenizer tokenizer) {
        super();
        this.tokenizer = tokenizer;
    }

    protected TokenizerToLuceneTokenizer(AttributeFactory factory, Tokenizer tokenizer) {
        super(factory);
        this.tokenizer = tokenizer;
    }

    @Override
    public void reset() throws IOException {
        super.reset();

        String documentText;
        try(java.util.Scanner s = new java.util.Scanner(input)) {
            documentText = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }

        SimpleDocument currentDocument = new SimpleDocument(documentText);

        tokenizer.tokenize(currentDocument);

        tokenIterator = currentDocument.getTokens().iterator();
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        if (tokenIterator.hasNext()) {
            Token next = tokenIterator.next();
            offsetAttribute.setOffset(correctOffset(next.getBegin()), correctOffset(next.getEnd()));
            charTermAttribute.append(next.getText());
        }

        return tokenIterator.hasNext();
    }

    @Override
    public void end() throws IOException {
        super.end();
    }
}
