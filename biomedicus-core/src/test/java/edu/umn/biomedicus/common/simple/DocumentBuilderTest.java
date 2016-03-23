package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Document;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link DocumentBuilder}.
 */
public class DocumentBuilderTest {
    DocumentBuilder documentBuilder;

    @BeforeMethod
    public void setUp() throws Exception {
        documentBuilder = new DocumentBuilder();
    }

    @Test
    public void testToken() throws Exception {
        documentBuilder.token("test").token(" doc");
        Document document = documentBuilder.build();
        assertEquals(document.getText(), "test doc", "Token should append to document.");
    }

    @Test
    public void testSpace() throws Exception {
        documentBuilder.space();
        Document document = documentBuilder.build();
        assertEquals(document.getText(), " ", "Space should append a single space to document.");
    }

    @Test
    public void testBeginSentence() throws Exception {
        documentBuilder.space();
        documentBuilder.beginSentence();
        documentBuilder.token("test");
        documentBuilder.endSentence();
        Document document = documentBuilder.build();
        assertEquals(document.getSentences().iterator().next().getBegin(), 1, "beginSentence should start a sentence.");

    }

    @Test
    public void testEndSentence() throws Exception {
        documentBuilder.token("test");
        documentBuilder.endSentence();
        Document document = documentBuilder.build();
        assertEquals(document.getSentences().iterator().next().getEnd(), 4, "endSentence should end a sentence.");
    }
}