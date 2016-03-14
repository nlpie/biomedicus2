package edu.umn.biomedicus.common.terms;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A metric tree which uses Edit distance as keys. It is based on the paper "Some approaches to best-match file
 * searching" by W.A. Burkhard and R.M. Keller.
 * <a href="http://dl.acm.org/citation.cfm?doid=362003.362025">http://dl.acm.org/citation.cfm?doid=362003.362025</a>
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class BKTree {
    private final Node rootNode;

    private final EditDistance editDistance;

    private BKTree(Node rootNode, EditDistance editDistance) {
        this.rootNode = rootNode;
        this.editDistance = editDistance;
    }

    private BKTree(MutableNode rootNode, EditDistance editDistance) {
        this.rootNode = rootNode.build(this);
        this.editDistance = editDistance;
    }

    public Stream<String> search(String word, int maxDistance) {
        return rootNode.search(word, maxDistance);
    }

    private class Node {
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
            int distance = editDistance.compute(query, word);
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

        private Node build(BKTree bkTree) {
            int childrenSize = children.size();
            if (childrenSize == 0) {
                return bkTree.new Node(word, null, null);
            }

            List<Map.Entry<Integer, MutableNode>> sorted = sortChildrenByDistance();

            int[] distances = new int[childrenSize];
            Node[] nodes = new Node[childrenSize];
            for (int i = 0; i < childrenSize; i++) {
                Map.Entry<Integer, MutableNode> entry = sorted.get(i);
                distances[i] = entry.getKey();
                nodes[i] = entry.getValue().build(bkTree);
            }
            return bkTree.new Node(word, distances, nodes);
        }

        private List<Map.Entry<Integer, MutableNode>> sortChildrenByDistance() {
            return children
                    .entrySet()
                    .stream()
                    .sorted((first, second) -> first.getKey().compareTo(second.getKey()))
                    .collect(Collectors.toList());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private MutableNode rootNode = null;

        @Nullable
        private EditDistance editDistance;

        private Builder() {

        }

        public Builder withEditDistance(EditDistance editDistance) {
            this.editDistance = editDistance;
            return this;
        }

        public Builder add(String word) {
            if (editDistance == null) {
                throw new IllegalStateException("Edit distance method needs to be set before adding words");
            }

            if (rootNode == null) {
                rootNode = new MutableNode(word);
            }

            MutableNode currentNode = rootNode;

            while (true) {
                int distance = editDistance.compute(currentNode.word, word);
                if (distance == 0) {
                    break;
                }
                if (!currentNode.children.containsKey(distance)) {
                    currentNode.children.put(distance, new MutableNode(word));
                    break;
                }

                currentNode = currentNode.children.get(distance);
            }
            return this;
        }

        public BKTree build() {
            if (rootNode == null) {
                throw new IllegalStateException("Empty BK Tree");
            }
            if (editDistance == null) {
                throw new IllegalStateException("Edit distance method needs to be set before adding words");
            }
            return new BKTree(rootNode, editDistance);
        }
    }
}
