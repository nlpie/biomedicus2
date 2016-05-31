package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.*;
import mockit.*;
import org.testng.annotations.Test;

import java.io.Reader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link SimpleDocument}.
 */
public class SimpleDocumentTest {
    @Tested SimpleDocument simpleDocument;

    @Injectable(value = "document text") String documentText;

    @Injectable List<Token> tokenList;

    @Injectable List<Sentence> sentenceList;

    @Test
    public void testConstructor() throws Exception {
        SimpleDocument simpleDocument = new SimpleDocument("document text");

        assertEquals(simpleDocument.getText(), "document text");
    }

    @Test
    public void testGetTokens() throws Exception {
        assertEquals(simpleDocument.getTokens(), tokenList);
    }

    @Test
    public void testGetSentences() throws Exception {
        assertEquals(simpleDocument.getSentences(), sentenceList);
    }

    @Test
    public void testAddSentence(@Injectable SpanLike spanLike, @Mocked Token token, @Mocked SimpleSentence sentence) throws Exception {
        new Expectations() {{
            spanLike.getBegin(); result = 10;
            spanLike.getEnd(); result = 15;
            tokenList.stream(); result = Stream.of(token, token, token);
            token.getBegin(); returns(5, 6, 10);
            token.getEnd(); returns(6, 10, 15);
        }};

        simpleDocument.createSentence(spanLike);

        new Verifications() {{
            List<Token> tokens;
            new SimpleSentence("document text", 10, 15, tokens = withCapture());
            sentenceList.add(withAny(sentence));
            assertEquals(tokens.size(), 1);
        }};
    }

    @Test
    public void testGetTerms(@Injectable List<SimpleTerm> simpleTerms) throws Exception {
        Deencapsulation.setField(simpleDocument, "terms", simpleTerms);

        assertEquals(simpleDocument.getSimpleTerms(), simpleTerms);
    }

    @Test
    public void testAddTerm(@Injectable List<SimpleTerm> simpleTerms, @Injectable SimpleTerm simpleTerm) throws Exception {
        Deencapsulation.setField(simpleDocument, "terms", simpleTerms);

        simpleDocument.addTerm(simpleTerm);

        new Verifications() {{
            simpleTerms.add(simpleTerm);
        }};
    }

    @Test
    public void testGetReader() throws Exception {
        Reader reader = simpleDocument.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            stringBuilder.append((char) ch);
        }

        assertEquals(stringBuilder.toString(), "document text");
    }

    @Test
    public void testAddToken(@Injectable SpanLike spanLike) throws Exception {
        new Expectations() {{
            spanLike.getBegin(); result = 0;
            spanLike.getEnd(); result = 8;
        }};

        simpleDocument.createToken(spanLike);

        new Verifications() {{
            Token token;
            tokenList.add(token = withCapture());
            assertEquals(token.getText(), "document");
            assertEquals(token.getBegin(), 0);
            assertEquals(token.getEnd(), 8);
        }};
    }

    @Test
    public void testGetText() throws Exception {
        assertEquals(simpleDocument.getText(), "document text");
    }

    @Test
    public void testSetCategory() throws Exception {
        simpleDocument.setCategory("a category");

        assertEquals(Deencapsulation.getField(simpleDocument, "category"), "a category");
    }

    @Test
    public void testGetCategory() throws Exception {
        Deencapsulation.setField(simpleDocument, "category", "a category");

        assertEquals(simpleDocument.getCategory(), "a category");
    }

    @Test
    public void testGetIdentifier() throws Exception {
        String identifier = simpleDocument.getIdentifier();

        UUID uuid = UUID.fromString(identifier);
        assertNotNull(uuid);
    }

    @Test
    public void testTextSegments() throws Exception {
        List<TextSpan> collect = simpleDocument.textSegments().collect(Collectors.toList());

        assertEquals(collect.size(), 1);
        assertEquals(collect.get(0).getText(), "document text");
    }

    @Test
    public void testBeginEditing() throws Exception {
        simpleDocument.beginEditing();
    }

    @Test
    public void testEndEditing() throws Exception {
        simpleDocument.endEditing();
    }
}