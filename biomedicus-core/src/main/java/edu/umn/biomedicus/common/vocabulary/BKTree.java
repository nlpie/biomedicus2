package edu.umn.biomedicus.common.vocabulary;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A metric tree which uses Levenshtein distance as keys. It is based on the paper "Some approaches to best-match file
 * searching" by W.A. Burkhard and R.M. Keller.
 * <a href="http://dl.acm.org/citation.cfm?doid=362003.362025">http://dl.acm.org/citation.cfm?doid=362003.362025</a>
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class BKTree {
    private final Node rootNode;

    private BKTree(Node rootNode) {
        this.rootNode = rootNode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Stream<String> search(String word, int maxDistance) {
        return rootNode.search(word, maxDistance);
    }

    private static class Node {
        private final String word;

        @Nullable
        private final int[] childDistances;

        @Nullable
        private final Node[] children;

        private Node(String word, @Nullable int[] childDistances, @Nullable Node[] children) {
            this.word = word;
            this.childDistances = childDistances;
            this.children = children;
        }

        private Stream<String> search(String query, int maxDistance) {
            int distance = new EditDistance(Costs.LEVENSHTEIN, query, word).compute();
            int min = distance - maxDistance;
            int max = distance + maxDistance;

            Stream<String> returnStream = childrenBetween(min, max).flatMap(child -> child.search(query, maxDistance));
            if (distance <= maxDistance) {
                returnStream = Stream.concat(Stream.of(word), returnStream);
            }
            return returnStream;
        }

        private Stream<Node> childrenBetween(int min, int max) {
            if (childDistances == null || children == null) {
                return Stream.empty();
            }
            int minIndex = Math.abs(Arrays.binarySearch(childDistances, min));
            int maxIndex = Math.abs(Arrays.binarySearch(childDistances, minIndex, childDistances.length, max));
            return Arrays.stream(children, minIndex, maxIndex);
        }
    }

    private static class MutableNode {
        private final String word;

        private final Map<Integer, MutableNode> children = new HashMap<>();

        private MutableNode(String word) {
            this.word = word;
        }

        private Node build() {
            int childrenSize = children.size();
            if (childrenSize == 0) {
                return new Node(word, null, null);
            }

            List<Map.Entry<Integer, MutableNode>> sorted = sortChildrenByDistance();

            int[] distances = new int[childrenSize];
            Node[] nodes = new Node[childrenSize];
            for (int i = 0; i < childrenSize; i++) {
                Map.Entry<Integer, MutableNode> entry = sorted.get(i);
                distances[i] = entry.getKey();
                nodes[i] = entry.getValue().build();
            }
            return new Node(word, distances, nodes);
        }

        private List<Map.Entry<Integer, MutableNode>> sortChildrenByDistance() {
            return children
                    .entrySet()
                    .stream()
                    .sorted((first, second) -> first.getKey().compareTo(second.getKey()))
                    .collect(Collectors.toList());
        }
    }

    public static class Builder {
        private MutableNode rootNode = null;

        private Builder() {

        }

        public void add(String word) {
            if (rootNode == null) {
                rootNode = new MutableNode(word);
            }

            MutableNode currentNode = rootNode;

            while (true) {
                int distance = new EditDistance(Costs.LEVENSHTEIN, currentNode.word, word).compute();
                if (distance == 0) {
                    break;
                }
                if (!currentNode.children.containsKey(distance)) {
                    currentNode.children.put(distance, new MutableNode(word));
                    break;
                }

                currentNode = currentNode.children.get(distance);
            }
        }

        public BKTree build() {
            return new BKTree(rootNode.build());
        }
    }
}
