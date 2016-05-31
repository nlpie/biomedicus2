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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;
import mockit.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegexSentenceSplitterTest {
    @Tested
    RegexSentenceSplitter regexSentenceSplitter;
    @Injectable Pattern pattern;
    @Mocked Matcher matcher;

    @Test
    public void testSetDocumentText() throws Exception {
        String documentText = "this is some document text";

        regexSentenceSplitter.setDocumentText(documentText);

        Assert.assertEquals(documentText, Deencapsulation.getField(regexSentenceSplitter, "documentText"));
    }

    @Test
    public void testSplitCandidate() throws Exception {
        String documentText = "this is some document text. here.";
        Deencapsulation.setField(regexSentenceSplitter, "documentText", documentText);
        new StrictExpectations() {{
            pattern.matcher(documentText.substring(5)); result = matcher;
            matcher.find(); result = true;
            matcher.end(); result = 22;
            matcher.find(); result = false;
        }};

        Stream<SpanLike> spanStream = regexSentenceSplitter.splitCandidate(Span.spanning(5, 33));
        List<SpanLike> collect = spanStream.collect(Collectors.toList());
        Assert.assertEquals(2, collect.size());
        Assert.assertEquals(Span.spanning(5, 27), collect.get(0));
        Assert.assertEquals(Span.spanning(27, 33), collect.get(1));
    }
}