package edu.umn.biomedicus.common.terms;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

/**
 *
 */
public class DirectedAcyclicWordGraphTest {
    private final List<String> words = Arrays.asList(
            "postspan",
            "postspanning",
            "prespan",
            "prespanning",
            "span",
            "spanning");
    DirectedAcyclicWordGraph directedAcyclicWordGraph = DirectedAcyclicWordGraph.builder(MappedCharacterSet.builder().addAll(IntStream.range('a', 'z').mapToObj(c -> Character.toString((char) c)).collect(Collectors.joining())).build())
            .addAll(words)
            .build();

    @Test
    public void testGetIndex() throws Exception {
        assertEquals(directedAcyclicWordGraph.indexOf("postspan"), 0);
        assertEquals(directedAcyclicWordGraph.indexOf("postspanning"), 1);
        assertEquals(directedAcyclicWordGraph.indexOf("prespan"), 2);
        assertEquals(directedAcyclicWordGraph.indexOf("prespanning"), 3);
        assertEquals(directedAcyclicWordGraph.indexOf("span"), 4);
        assertEquals(directedAcyclicWordGraph.indexOf("spanning"), 5);
    }

    @Test
    public void testGet() throws Exception {
        assertEquals(directedAcyclicWordGraph.forIndex(0), "postspan");
        assertEquals(directedAcyclicWordGraph.forIndex(1), "postspanning");
        assertEquals(directedAcyclicWordGraph.forIndex(2), "prespan");
        assertEquals(directedAcyclicWordGraph.forIndex(3), "prespanning");
        assertEquals(directedAcyclicWordGraph.forIndex(4), "span");
        assertEquals(directedAcyclicWordGraph.forIndex(5), "spanning");
    }

    @Test
    public void testContains() {
        assertEquals(directedAcyclicWordGraph.contains("postspan"), true);
        assertEquals(directedAcyclicWordGraph.contains("postspanning"), true);
        assertEquals(directedAcyclicWordGraph.contains("prespan"), true);
        assertEquals(directedAcyclicWordGraph.contains("prespanning"), true);
        assertEquals(directedAcyclicWordGraph.contains("span"), true);
        assertEquals(directedAcyclicWordGraph.contains("spanning"), true);
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(directedAcyclicWordGraph.size(), 6);
    }

    @Test
    public void testIterator() throws Exception {
        DirectedAcyclicWordGraph.DAWGIterator DAWGIterator = directedAcyclicWordGraph.new DAWGIterator();
        List<String> seen = new ArrayList<>();
        while (DAWGIterator.hasNext()) {
            String next = DAWGIterator.next();
            assertTrue(words.contains(next), "Not valid: " + next);
            assertFalse(seen.contains(next), "Already seen: " + next);
            seen.add(next);
        }

        assertEquals(seen.size(), 6);
    }

    @Test
    public void testMinimal() throws Exception {
        DirectedAcyclicWordGraph.NodeIterator nodeIterator = directedAcyclicWordGraph.new NodeIterator();
        HashSet<DirectedAcyclicWordGraph.Node> nodes = new HashSet<>();
        while (nodeIterator.hasNext()) {
            DirectedAcyclicWordGraph.Node node = nodeIterator.next();
            nodes.add(node);
        }
        assertEquals(nodes.size(), 14);
    }
}