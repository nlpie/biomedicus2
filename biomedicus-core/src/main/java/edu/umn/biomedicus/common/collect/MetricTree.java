package edu.umn.biomedicus.common.collect;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
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
public class MetricTree<T> {
    private final Node<T> rootNode;

    private final Metric<T> metric;

    private MetricTree(Node<T> rootNode, Metric<T> metric) {
        this.rootNode = rootNode;
        this.metric = metric;
    }

    private MetricTree(MutableNode<T> rootNode, Metric<T> metric) {
        this.rootNode = rootNode.build(this);
        this.metric = metric;
    }

    public Stream<T> search(T word, int maxDistance) {
        return rootNode.search(metric, word, maxDistance);
    }

    private static class Node<T> {
        private final T word;

        @Nullable
        private final int[] childDistances;

        @Nullable
        private final Node<T>[] children;

        private Node() {
            word = null;
            childDistances = null;
            children = null;
        }

        private Node(T word, @Nullable int[] childDistances, @Nullable Node<T>[] children) {
            this.word = word;
            this.childDistances = childDistances;
            this.children = children;
        }

        private Stream<T> search(Metric<T> metric, T query, int maxDistance) {
            int distance = metric.compute(query, word);
            int min = distance - maxDistance;
            int max = distance + maxDistance;

            Stream<T> returnStream = childrenBetween(min, max).flatMap(child -> child.search(metric, query, maxDistance));
            if (distance <= maxDistance) {
                returnStream = Stream.concat(Stream.of(word), returnStream);
            }
            return returnStream;
        }

        private Stream<Node<T>> childrenBetween(int min, int max) {
            if (childDistances == null || children == null) {
                return Stream.empty();
            }
            int minIndex = Math.abs(Arrays.binarySearch(childDistances, min));
            int maxIndex = Math.abs(Arrays.binarySearch(childDistances, minIndex, childDistances.length, max));
            return Arrays.stream(children, minIndex, maxIndex);
        }
    }

    private static class MutableNode<T> {
        private final T word;

        private final Map<Integer, MutableNode<T>> children = new HashMap<>();

        private MutableNode(T word) {
            this.word = word;
        }

        private Node<T> build(MetricTree<T> metricTree) {
            int childrenSize = children.size();
            if (childrenSize == 0) {
                return new Node<T>(word, null, null);
            }

            List<Map.Entry<Integer, MutableNode<T>>> sorted = sortChildrenByDistance();

            int[] distances = new int[childrenSize];
            @SuppressWarnings("unchecked")
            Node<T>[] nodes = (Node<T>[]) Array.newInstance((new Node<T>()).getClass(), childrenSize);
            for (int i = 0; i < childrenSize; i++) {
                Map.Entry<Integer, MutableNode<T>> entry = sorted.get(i);
                distances[i] = entry.getKey();
                nodes[i] = entry.getValue().build(metricTree);
            }
            return new Node<>(word, distances, nodes);
        }

        private List<Map.Entry<Integer, MutableNode<T>>> sortChildrenByDistance() {
            return children
                    .entrySet()
                    .stream()
                    .sorted((first, second) -> first.getKey().compareTo(second.getKey()))
                    .collect(Collectors.toList());
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        @Nullable
        private MutableNode<T> rootNode = null;

        @Nullable
        private Metric<T> metric;

        private Builder() {

        }

        public Builder<T> withMetric(Metric<T> metric) {
            this.metric = metric;
            return this;
        }

        public Builder<T> add(T word) {
            if (metric == null) {
                throw new IllegalStateException("Edit distance method needs to be set before adding words");
            }

            if (rootNode == null) {
                rootNode = new MutableNode<>(word);
            }

            MutableNode<T> currentNode = rootNode;

            while (true) {
                int distance = metric.compute(currentNode.word, word);
                if (distance == 0) {
                    break;
                }
                if (!currentNode.children.containsKey(distance)) {
                    currentNode.children.put(distance, new MutableNode<>(word));
                    break;
                }

                currentNode = currentNode.children.get(distance);
            }
            return this;
        }

        public MetricTree<T> build() {
            if (rootNode == null) {
                throw new IllegalStateException("Empty BK Tree");
            }
            if (metric == null) {
                throw new IllegalStateException("Edit distance method needs to be set before adding words");
            }
            return new MetricTree<T>(rootNode, metric);
        }
    }
}
