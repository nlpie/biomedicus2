package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.Token;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleToken}.
 */
public class SimpleTokenTest {
    SimpleToken simpleToken;

    SimpleToken other;

    @BeforeMethod
    public void setUp() throws Exception {
        simpleToken = new SimpleToken("document text.", 0, 8);
        other = new SimpleToken("document text.", 0, 8);
    }

    @Test
    public void testPartOfSpeech() throws Exception {
        simpleToken.setPennPartOfSpeech(PartOfSpeech.CD);
        assertEquals(simpleToken.getPartOfSpeech(), PartOfSpeech.CD, "Should retrieve set part of speech.");
    }

    @Test
    public void testNormalForm() throws Exception {
        simpleToken.setNormalForm("document");
        assertEquals(simpleToken.getNormalForm(), "document", "Should retrieve set normal form.");
    }

    @Test
    public void testIsStopword() throws Exception {
        simpleToken.setIsStopword(false);
        assertEquals(simpleToken.isStopword(), false, "Should retrieve set stopword.");
    }

    @Test
    public void testIsMisspelled() throws Exception {
        simpleToken.setIsMisspelled(false);
        assertEquals(simpleToken.isMisspelled(), false, "Should retrieve set isMisspelled.");
    }

    @Test
    public void testCorrectSpelling() throws Exception {
        simpleToken.setCorrectSpelling("Document");
        assertEquals(simpleToken.getCorrectText(), "Document", "Should retrieve set correctSpelling.");
    }

    @Test
    public void testFromSpan() throws Exception {
        Token token = SimpleToken.fromSpan(new Span(0, 8), "document text.");
        assertEquals(token, simpleToken, "From span should create equivalent span.");
    }

    @Test
    public void testBeginEditing() throws Exception {
        simpleToken.beginEditing();
    }

    @Test
    public void testEndEditing() throws Exception {
        simpleToken.endEditing();
    }

    @Test
    public void testEqualsNull() throws Exception {
        assertFalse(simpleToken.equals(null), "Token should not equal null.");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        assertTrue(simpleToken.equals(simpleToken), "Token should be equal when same object.");
    }

    @Test
    public void testEqualsDifferentIsMisspelled() throws Exception {
        other.setIsMisspelled(true);
        assertFalse(simpleToken.equals(other), "Token should not be equal with different misspelled values");
    }

    @Test
    public void testEqualsDifferentIsStopword() throws Exception {
        other.setIsStopword(true);
        assertFalse(simpleToken.equals(other), "Token should not be equal with different isStopword values.");
    }

    @Test
    public void testEqualsDifferentCorrectedSpelling() throws Exception {
        simpleToken.setCorrectSpelling("Document");
        other.setCorrectSpelling("document");
        assertFalse(simpleToken.equals(other), "Token should not be equal with different correctSpelling values.");
    }

    @Test
    public void testEqualsDifferentNormalForm() throws Exception {
        simpleToken.setNormalForm("first");
        other.setNormalForm("other");
        assertFalse(simpleToken.equals(other), "Token should not be equal with different normalForm values");
    }

    @Test
    public void testEqualsDifferentPartOfSpeech() throws Exception {
        simpleToken.setPennPartOfSpeech(PartOfSpeech.CC);
        other.setPennPartOfSpeech(PartOfSpeech.CD);
        assertFalse(simpleToken.equals(other), "Token should not be equal with different partOfSpeech values");
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(simpleToken.equals(other), "Token should be equal with same values");
    }

    @Test
    public void testHashCode() throws Exception {
        assertTrue(simpleToken.hashCode() == other.hashCode(), "Token should have same hash code with equal values");
    }

    @Test
    public void testHashCodeNeq() throws Exception {
        other.setNormalForm("normal");
        assertFalse(simpleToken.hashCode() == other.hashCode(),
                "Token should have different hash code with different values");
    }

    @Test
    public void testToString() throws Exception {
        String string = simpleToken.toString();
        assertTrue(string.contains("SimpleToken") && string.contains("correctedSpelling='null'")
                && string.contains("normalForm='null'") && string.contains("normalForm='null'")
                && string.contains("partOfSpeech=null") && string.contains("isMisspelled=false")
                && string.contains("isStopword=false"));
    }
}