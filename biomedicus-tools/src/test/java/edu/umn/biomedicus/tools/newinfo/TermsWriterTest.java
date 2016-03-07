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

import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.common.text.Token;
import mockit.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for {@link TermsWriter}.
 */
public class TermsWriterTest {
    @Tested TermsWriter termsWriter;

    @Injectable List<Term> terms;

    @Injectable Writer writer;

    @Mocked Token token;

    @Mocked Term term;

    @Test
    public void testIncrementsToFirstTerm() throws Exception {
        new Expectations() {{
            terms.size(); result = 100;
            terms.get(anyInt); result = term;
            token.getBegin(); result = 20;
            term.getEnd(); returns(5, 10, 15, 20);
        }};

        termsWriter.check(token, 10, 10);

        new Verifications() {{
            List<Integer> indices = new ArrayList<>();
            terms.get(withCapture(indices));
            Assert.assertEquals(Arrays.asList(new Integer[] {0, 1, 2, 3, 3}), indices);
        }};

        @SuppressWarnings("unchecked")
        int firstTermIndex = Deencapsulation.getField(termsWriter, "firstTermIndex");
        Assert.assertEquals(3, firstTermIndex);
    }

    @Test
    public void testNoTermsLeft() throws Exception {
        new Expectations() {{
            terms.size(); result = 0;
        }};

        termsWriter.check(token, 10, 10);

        new Verifications() {{
            terms.get(anyInt); times = 0;
        }};
    }

    @Test
    public void testCheck(@Mocked Concept concept, @Mocked TokenWithConceptLine tokenWithConceptLine) throws Exception {
        new Expectations() {{
            terms.size(); result = 1;
            token.getBegin(); result = 5;
            terms.get(anyInt); result = term;
            term.getEnd(); returns(10, 10);
            token.getEnd(); result = 10;
            term.getBegin(); returns(4, 6);
            term.contains(token); result = true;
            term.getPrimaryConcept(); result = concept;
            concept.getType(); result = "T555";
            concept.getIdentifier(); result = "CXXX";
            new TokenWithConceptLine(10, 10, 0, "CXXX", "T555"); result = tokenWithConceptLine;
            tokenWithConceptLine.createLine(); result = "The line.";
        }};

        termsWriter.check(token, 10, 10);

        new Verifications() {{
            writer.write("The line.\n");
        }};
    }
}