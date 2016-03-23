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

import edu.umn.biomedicus.common.utilities.Resources;
import edu.umn.biomedicus.icu.BreakIteratorTokenizer;
import edu.umn.biomedicus.processing.Tokenizer;
import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

/**
 *
 */
public class TokenizingAnalyzer extends Analyzer {

    private final Tokenizer tokenizer;

    public TokenizingAnalyzer() throws IOException {
        this(GLOBAL_REUSE_STRATEGY);
    }

    public TokenizingAnalyzer(ReuseStrategy reuseStrategy) throws IOException {
        super(reuseStrategy);

        tokenizer = BreakIteratorTokenizer.createWithRules(Resources.toString("edu/umn/biomedicus/orthography/tokenization/tokenizer_break_rules.txt"));
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        org.apache.lucene.analysis.Tokenizer luceneTokenizer = new TokenizerToLuceneTokenizer(tokenizer);

        return new TokenStreamComponents(luceneTokenizer);
    }
}
