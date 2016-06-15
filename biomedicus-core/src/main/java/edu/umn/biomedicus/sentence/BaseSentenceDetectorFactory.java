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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.utilities.Patterns;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract implementation of {@link edu.umn.biomedicus.sentence.SentenceDetectorFactory}.
 *
 *
 *
 * @author Ben Knoll
 */
public abstract class BaseSentenceDetectorFactory implements SentenceDetectorFactory {
    private static final Pattern ABBREVS;
    private static final Pattern SPLITS;

    static {
        ABBREVS = Patterns.loadPatternByJoiningLines("edu/umn/biomedicus/config/sentence/abbrevs.txt");
        SPLITS = Patterns.loadPatternByJoiningLines("edu/umn/biomedicus/config/sentence/splits.txt");
    }

    /**
     * The function used as a {@link edu.umn.biomedicus.processing.Preprocessor}. Takes anything that
     * matches the ABBREVS pattern field in this class and replaces it with an equal number of "x" characters.
     *
     * @param original the original document text
     * @return the document text with replacements
     */
    public static String preprocess(CharSequence original) {
        StringBuffer sb = new StringBuffer(original.length());
        Matcher matcher = ABBREVS.matcher(original);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            char[] array = new char[end - start];
            Arrays.fill(array, 'x');
            matcher.appendReplacement(sb, new String(array));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public SentenceDetector create() {
        SentenceCandidateGenerator candidateGenerator = getCandidateGenerator();

        RegexSentenceSplitter sentenceSplitter = new RegexSentenceSplitter(SPLITS);

        return new SentenceDetector(BaseSentenceDetectorFactory::preprocess, candidateGenerator, sentenceSplitter);
    }

    protected abstract SentenceCandidateGenerator getCandidateGenerator();
}
