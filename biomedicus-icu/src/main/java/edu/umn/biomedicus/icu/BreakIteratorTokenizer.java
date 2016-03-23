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

package edu.umn.biomedicus.icu;


import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import edu.umn.biomedicus.common.simple.SimpleTextSpan;
import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextSpan;
import edu.umn.biomedicus.processing.Tokenizer;

/**
 * Tokenizes a document using an IBM ICU {@link com.ibm.icu.text.RuleBasedBreakIterator}. Uses a
 * {@link java.lang.ThreadLocal} to store one break iterator per thread an instance is used from to ensure thread
 * safety.
 *
 * @author Ben Knoll
 * @see com.ibm.icu.text.RuleBasedBreakIterator
 * @since 1.1.0
 */
public class BreakIteratorTokenizer implements Tokenizer {
    private final ThreadLocal<BreakIterator> breakIteratorThreadLocal;

    /**
     * Default constructor. Creates with the single field of a {@link java.lang.ThreadLocal}
     * {@link com.ibm.icu.text.BreakIterator}. The ThreadLocal should override
     * {@link java.lang.ThreadLocal#initialValue} to provide a new instance of BreakIterator for each thread.
     *
     * @param breakIteratorThreadLocal thread local to store break iterators
     * @see #createWithRules
     * @see #createWithPrototype
     */
    public BreakIteratorTokenizer(ThreadLocal<BreakIterator> breakIteratorThreadLocal) {
        this.breakIteratorThreadLocal = breakIteratorThreadLocal;
    }

    /**
     * Creates a BreakIteratorTokenizer with the given rules string.
     *
     * @param rules rules string
     * @return new BreakIteratorTokenizer
     */
    public static BreakIteratorTokenizer createWithRules(String rules) {
        BreakIterator breakIterator = new RuleBasedBreakIterator(rules);
        return createWithPrototype(breakIterator);
    }

    /**
     * Creates a BreakIteratorTokenizer with the provided {@link com.ibm.icu.text.BreakIterator} prototype.
     *
     * @param breakIterator the prototype BreakIterator which will be cloned per thread
     * @return a new BreakIteratorTokenizer
     */
    public static BreakIteratorTokenizer createWithPrototype(BreakIterator breakIterator) {
        ThreadLocal<BreakIterator> threadLocal = new ThreadLocal<BreakIterator>() {
            @Override
            protected BreakIterator initialValue() {
                Object clone = breakIterator.clone();
                if (clone instanceof BreakIterator) {
                    return (BreakIterator) clone;
                } else {
                    throw new AssertionError("Clones of a BreakIterator should be a BreakIterator");
                }
            }
        };

        return new BreakIteratorTokenizer(threadLocal);
    }

    @Override
    public void tokenize(Document document) {
        String documentText = document.getText();

        BreakIterator breakIterator = breakIteratorThreadLocal.get();

        breakIterator.setText(documentText);

        int begin = breakIterator.first();
        int end = breakIterator.next();
        while (end != BreakIterator.DONE) {
            Span span = Spans.spanning(begin, end);
            TextSpan textSpan = new SimpleTextSpan(span, documentText);
            if (textSpan.containsNonWhitespace()) {
                document.createToken(span);
            }
            begin = end;
            end = breakIterator.next();
        }
    }
}