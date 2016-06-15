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

package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Token;
import mockit.Mocked;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleSentence}.
 */
public class SimpleSentenceTest {
    @Mocked Token token;

    SimpleSentence simpleSentence;

    @BeforeMethod
    public void setUp() throws Exception {
        simpleSentence = new SimpleSentence("document. text.", 10, 15, Arrays.asList(token, token, token));
    }

    @Test
    public void testGetTokens() throws Exception {
        List<Token> tokens = simpleSentence.getTokens();

        assertEquals(tokens, Arrays.asList(token, token, token), "getTokens should return list of three tokens");
    }

    @Test
    public void testTokens() throws Exception {
        List<Token> tokens = simpleSentence.tokens().collect(Collectors.toList());

        assertEquals(tokens, Arrays.asList(token, token, token), "tokens should return stream of three tokens");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetTermsStream() throws Exception {
        simpleSentence.terms();

        fail("terms should throw UnsupportedOperationException");
    }

    @Test
    public void testGetBegin() throws Exception {
        int begin = simpleSentence.getBegin();

        assertEquals(begin, 10, "getBegin should return value passed to constructor");
    }

    @Test
    public void testGetEnd() throws Exception {
        int end = simpleSentence.getEnd();

        assertEquals(end, 15, "getEnd should return value passed to constructor");
    }

    @Test
    public void testGetText() throws Exception {
        CharSequence text = simpleSentence.getText();

        assertEquals(text, "text.", "getText should return substring of documentText between begin and end");
    }

    @Test
    public void testEqualsSame() throws Exception {
        assertTrue(simpleSentence.equals(simpleSentence), "SimpleSentence same objects should be equal");
    }

    @Test
    public void testEqualsNull() throws Exception {
        assertFalse(simpleSentence.equals(null), "SimpleSentence should not be equal to null");
    }

    @Test
    public void testEqualsOtherClass() throws Exception {
        assertFalse(simpleSentence.equals("a string"),
                "SimpleSentence should not be equal to an object of different type");
    }

    @Test
    public void testEqualsDifferentBegin() throws Exception {
        SimpleSentence other = new SimpleSentence("doucment. text.", 9, 15, Arrays.asList(token, token, token));
        assertFalse(simpleSentence.equals(other), "SimpleSentences with different begins should not be equal");
    }

    @Test
    public void testEqualsTrue() throws Exception {
        SimpleSentence other = new SimpleSentence("document. text.", 10, 15, Arrays.asList(token, token, token));
        assertTrue(simpleSentence.equals(other), "SimpleSentence should be equal to identical object");
    }

    @Test
    public void testHashCodeNeq() throws Exception {
        SimpleSentence other = new SimpleSentence("document.", 0, 3, Arrays.asList(token, token));
        assertNotEquals(other.hashCode(), simpleSentence.hashCode(),
                "Different objects should have different hashes");
    }

    @Test
    public void testHashCodeEq() throws Exception {
        SimpleSentence other = new SimpleSentence("document. text.", 10, 15, Arrays.asList(token, token, token));
        assertEquals(other.hashCode(), simpleSentence.hashCode(),
                "Identical objects should have the same hash code");
    }

    @Test
    public void testToString() throws Exception {
        SimpleSentence other = new SimpleSentence("document. text.", 10, 15, Arrays.asList(token, token, token));
        String s = other.toString();
        assertTrue(s.contains("SimpleSentence{begin=10, end=15, tokens=") && s.contains("documentText='document. text.'"),
                "toString should contain begin, end and the documentText");
    }
}
