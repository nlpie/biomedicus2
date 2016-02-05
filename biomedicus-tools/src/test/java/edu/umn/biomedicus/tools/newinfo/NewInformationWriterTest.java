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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.text.Token;
import mockit.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Unit test for {@link NewInformationWriter}.
 */
public class NewInformationWriterTest {
    @Tested NewInformationWriter newInformationWriter;

    @Injectable Writer tokensWriter;

    @Injectable Writer sentencesWriter;

    @Injectable TermsWriter termsWriter;

    @Injectable Iterator<Sentence> sentenceIterator;

    @Injectable(value = "this is a document") String document;

    @Test
    public void testHasNextSentence() throws Exception {
        new Expectations() {{
            sentenceIterator.hasNext(); result = true;
        }};

        Assert.assertTrue(newInformationWriter.hasNextSentence());
    }

    @Test
    public void testWriteNextSentence(@Mocked Sentence sentence,
                                      @Mocked Token token,
                                      @Mocked TokenLine tokenLine,
                                      @Mocked SentenceLine sentenceLine) throws Exception {
        new Expectations() {{
            sentenceIterator.next(); result = sentence;
            sentence.getBegin(); result = 0;
            sentence.getTokens(); result = Arrays.asList(token, token, token);
            token.getText(); result = "word";
            new TokenLine(anyInt, anyInt, "word"); result = tokenLine;
            tokenLine.line(); result = "a line\n";
            sentence.getText(); result = "sentence text";
            new SentenceLine(anyInt, "sentence text"); result = sentenceLine;
            sentenceLine.line(); result = "sentence line\n";
        }};

        newInformationWriter.writeNextSentence();

        new Verifications() {{
            tokensWriter.write("a line\n"); times = 3;
            termsWriter.check(token, anyInt, anyInt); times = 3;
            sentencesWriter.write("sentence line\n"); times = 1;
        }};
        @SuppressWarnings("unchecked")
        int sentenceNumber = Deencapsulation.getField(newInformationWriter, "sentenceNumber");
        Assert.assertEquals(1, sentenceNumber);

        @SuppressWarnings("unchecked")
        int wordNumber = Deencapsulation.getField(newInformationWriter, "wordNumber");
        Assert.assertEquals(3, wordNumber);
    }

    @Test
    public void testBuilder(@Mocked Document document,
                            @Mocked NewInformationSentenceIterator newInformationSentenceIterator) throws Exception {
        new MockUp<NewInformationWriter>() {
            public void $init(Writer tokensWriter, Writer sentencesWriter, TermsWriter termsWriter, Iterator<Sentence> sentenceIterator) {

            }
        };

        new Expectations() {{
            NewInformationSentenceIterator.create(document); result = newInformationSentenceIterator;
        }};

        NewInformationWriter.Builder builder = NewInformationWriter.builder();
        builder.withDocument(document);
        builder.withSentencesWriter(sentencesWriter);
        builder.withTermsWriter(termsWriter);
        builder.withTokensWriter(tokensWriter);
        builder.build();

        new Verifications(){{
            Deencapsulation.newInstance(NewInformationWriter.class, tokensWriter, sentencesWriter, termsWriter, newInformationSentenceIterator);
        }};
    }
}