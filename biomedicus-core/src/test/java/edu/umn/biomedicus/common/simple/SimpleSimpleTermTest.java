package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.semantics.Concept;
import mockit.MockUp;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleTerm}.
 */
public class SimpleSimpleTermTest {
    Concept concept = new MockUp<Concept>() {
    }.getMockInstance();

    SimpleTerm simpleSimpleTerm = new SimpleTerm(10, 15, concept, Arrays.asList(concept, concept));

    @Test
    public void testGetPrimaryConcept() throws Exception {
        assertEquals(simpleSimpleTerm.getPrimaryConcept(), concept, "Concept should be concept passed to constructor");
    }

    @Test
    public void testGetAlternativeConcepts() throws Exception {
        assertEquals(simpleSimpleTerm.getAlternativeConcepts(), Arrays.asList(concept, concept),
                "Alternative concepts should be equal to alternative concepts passed to constructor");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        assertTrue(simpleSimpleTerm.equals(simpleSimpleTerm), "Should be equal to itself");
    }

    @Test
    public void testEqualsNull() throws Exception {
        assertFalse(simpleSimpleTerm.equals(null), "Shouldn't be equal to null");
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(simpleSimpleTerm.equals(new SimpleTerm(10, 15, concept, Arrays.asList(concept, concept))),
                "Should be equal to identical object");
    }

    @Test
    public void testHashCodeNeq() throws Exception {
        assertFalse(simpleSimpleTerm.hashCode() == new SimpleTerm(0, 5, concept, Arrays.asList(concept, concept)).hashCode(),
                "Different objects should produce different hash codes");
    }

    @Test
    public void testHashCodeEq() throws Exception {
        assertTrue(simpleSimpleTerm.hashCode() == new SimpleTerm(10, 15, concept, Arrays.asList(concept, concept)).hashCode(),
                "Equivalent objects should produce same hash code");
    }

    @Test
    public void testName() throws Exception {
        String s = simpleSimpleTerm.toString();
        assertTrue(s.contains("SimpleTerm") && s.contains("begin=10") && s.contains("end=15") && s.contains("concept=")
                && s.contains("alternativeConcepts="), "toString should contain all fields");
    }
}