package edu.umn.biomedicus.common.grams;

import org.testng.annotations.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Unit test for {@link Ngram}.
 */
public class NgramTest {

    Ngram<String> bigram = Ngram.create("one", "two");
    Ngram<String> trigram = Ngram.create("first", "second", "third");
    Ngram<String> arrayBi = Ngram.bigram(new String[]{"a", "b", "c", "d", "e"}, 2);
    Ngram<String> arrayTri = Ngram.trigram(new String[]{"a", "b", "c", "d", "e"}, 2);

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testTailBigram() throws Exception {
        bigram.tail();
        fail("Tail should exception out by this point");
    }

    @Test
    public void testTail() throws Exception {
        Ngram<String> tail = arrayTri.tail();
        assertEquals(tail, Ngram.create("d", "e"), "Tail should return a bigram of the final two objects of a trigram");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testHeadBigram() throws Exception {
        bigram.head();
        fail("Head should exception out by this point");
    }

    @Test
    public void testHead() throws Exception {
        Ngram<String> head = arrayTri.head();
        assertEquals(head, Ngram.create("c", "d"), "Head should return a bigram of the first two objects of a trigram");
    }

    @Test
    public void testGetFirst() throws Exception {
        String first = arrayBi.getFirst();
        assertEquals(first, "c", "First should return the first object in a ngram");
    }

    @Test
    public void testGetSecond() throws Exception {
        String second = arrayBi.getSecond();
        assertEquals(second, "d", "Second should return the second object in a ngram");
    }

    @Test
    public void testGetThird() throws Exception {
        String third = trigram.getThird();
        assertEquals(third, "third", "Third should return the third object in a trigram");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetThirdBigram() throws Exception {
        bigram.getThird();
        fail("Should have thrown exception");
    }

    @Test
    public void testCompareToDifferentLength() throws Exception {
        assertNotEquals(bigram.compareTo(trigram), 0, "Different length ngrams should not be equal when compared");
    }

    @Test
    public void testCompareToDifferentValues() throws Exception {
        assertNotEquals(bigram.compareTo(arrayBi), 0,
                "Ngrams containing different values should not be equal when compared");
    }

    @Test
    public void testCompareToEquals() throws Exception {
        assertEquals(bigram.compareTo(Ngram.create("one", "two")), 0, "Equal bigrams should return 0 when compared");
    }

    @Test
    public void testIterator() throws Exception {
        List<String> list = new ArrayList<>();
        arrayBi.iterator().forEachRemaining(list::add);
        assertEquals(list, Arrays.asList("c", "d"));
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testIteratorBeyond() throws Exception {
        Iterator<String> iterator = arrayBi.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        fail("Should have failed on the third called to next");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        assertTrue(arrayTri.equals(arrayTri), "Object should be equal to itself");
    }

    @Test
    public void testEqualsNull() throws Exception {
        assertFalse(arrayTri.equals(null), "Object should not equal null.");
    }

    @Test
    public void testEqualsOtherClass() throws Exception {
        assertFalse(arrayTri.equals("string"), "Object should never equal another class");
    }

    @Test
    public void testEqualsDifferentLength() throws Exception {
        assertFalse(arrayTri.equals(arrayBi), "Ngram should not equal ngram with different length");
    }

    @Test
    public void testEqualsDifferentValues() throws Exception {
        assertFalse(arrayTri.equals(trigram), "Ngram should not equal ngram with different values");
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(arrayTri.equals(Ngram.create("c", "d", "e")), "Ngram should be equal to ngram with same values");
    }

    @Test
    public void testHashCodeNeq() throws Exception {
        assertNotEquals(arrayTri.hashCode(), arrayBi.hashCode(),
                "hash code for ngram with different values should not be equal");
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(arrayTri.hashCode(), Ngram.create("c", "d", "e").hashCode(),
                "hash code for objects with same value should be equal");
    }

    @Test
    public void testSerialization() throws Exception {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(pipedOutputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(pipedInputStream);

        objectOutputStream.writeObject(arrayTri);
        @SuppressWarnings("unchecked")
        Ngram<String> ngram = (Ngram<String>) objectInputStream.readObject();

        assertEquals(ngram, arrayTri, "Deserialized object should be same as serialized object.");
    }
}