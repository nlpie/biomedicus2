package edu.umn.biomedicus.common.terms;

import javax.annotation.Nullable;
import java.util.*;

/**
 *
 */
public class DirectedAcyclicWordGraph {
    private final MappedCharacterSet characterSet;

    private final Node graph;

    private DirectedAcyclicWordGraph(MappedCharacterSet characterSet) {
        this.characterSet = characterSet;
        graph = new Node();
    }

    class Node implements Comparable<Node> {
        private Node[] children;
        private int words;
        private boolean isFinal;

        Node() {
            children = new Node[characterSet.size()];
            words = 0;
            isFinal = false;
        }

        @Nullable
        public Node getChild(char c) {
            return children[characterSet.indexOf(c)];
        }

        void setChild(char c, Node node) {
            children[characterSet.indexOf(c)] = node;
        }

        int indexUpTo(char c) {
            return Arrays.stream(children, 0, characterSet.indexOf(c))
                    .filter(Objects::nonNull)
                    .mapToInt(child -> child.words)
                    .sum();
        }

        Node[] getChildren() {
            return children;
        }

        int getWords() {
            return words;
        }

        void setWords(int words) {
            this.words = words;
        }

        boolean isFinal() {
            return isFinal;
        }

        void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (isFinal != node.isFinal) return false;
            for (int i = 0; i < children.length; i++) {
                if (children[i] != node.children[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i = 0; i < children.length; i++) {
                result = 31 * result + System.identityHashCode(children[i]);
            }
            result = 31 * result + (isFinal ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.identityHashCode(this)).append("{isFinal:").append(isFinal).append(" words:").append(words).append(" ");
            for (int i = 0; i < characterSet.size(); i++) {
                Node node = children[i];
                if (node != null) {
                    Character character = characterSet.forIndex(i);
                    stringBuilder.append(character);
                    stringBuilder.append(" -> ");
                    stringBuilder.append(System.identityHashCode(node));
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append("}");

            return stringBuilder.toString();
        }

        @Override
        public int compareTo(Node o) {
            int compare = Boolean.compare(isFinal, o.isFinal);
            if (compare != 0) {
                return compare;
            }
            for (int i = 0; i < children.length; i++) {
                compare = Integer.compare(System.identityHashCode(children[i]), System.identityHashCode(o.children[i]));
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }
    }

    public int indexOf(CharSequence charSequence) {
        int index = 0;
        Node currentNode = graph;
        for (int i = 0; i < charSequence.length(); i++) {
            char currentChar = charSequence.charAt(i);
            if (currentNode.isFinal) {
                index++;
            }
            Node child = currentNode.getChild(currentChar);
            if (child != null) {
                index += currentNode.indexUpTo(currentChar);
                currentNode = child;
            } else {
                return -1;
            }
        }

        if (currentNode.isFinal) {
            return index;
        } else {
            return -1;
        }
    }

    public String forIndex(int index) {
        int runningIndex = index;
        StringBuilder builder = new StringBuilder();
        Node currentNode = graph;
        while (true) {
            if (currentNode.isFinal) {
                if (runningIndex == 0) {
                    return builder.toString();
                }
                runningIndex--;
            }
            Node[] children = currentNode.children;
            for (int i = 0; i < children.length; i++) {
                Node child = children[i];
                if (child != null) {
                    int childWords = child.getWords();
                    if (runningIndex < childWords) {
                        currentNode = child;
                        Character character = characterSet.forIndex(i);
                        builder.append(character);
                        break;
                    } else {
                        runningIndex -= childWords;
                    }
                }
            }
        }
    }

    public int size() {
        return graph.words;
    }

    public boolean contains(CharSequence charSequence) {
        Node currentNode = graph;
        for (int i = 0; i < charSequence.length(); i++) {
            char currentChar = charSequence.charAt(i);
            Node child = currentNode.getChild(currentChar);
            if (child != null) {
                currentNode = child;
            } else {
                return false;
            }
        }

        return currentNode.isFinal;
    }

    public static Builder builder(MappedCharacterSet characterSet) {
        return new DirectedAcyclicWordGraph(characterSet).new Builder();
    }

    public class Builder {
        private final Map<Node, Node> nodes = new HashMap<>();

        private final Node finalNode = new Node();
        {
            finalNode.isFinal = true;
            finalNode.words = 1;
            nodes.put(finalNode, finalNode);
        }

        public Builder addWord(CharSequence word) {
            if (contains(word)) {
                return this;
            }

            if (word.length() == 0) {
                nodes.remove(graph);
                graph.isFinal = true;
                nodes.put(graph, graph);
                return this;
            }

            Node currentNode = graph;
            List<Node> trail = new ArrayList<>(word.length() + 1);
            trail.add(graph);
            for (int i = 0; i < word.length() - 1; i++) {
                char c = word.charAt(i);
                Node next = currentNode.getChild(c);
                boolean nextFinal = next == finalNode;
                if (next == null || nextFinal) {
                    next = new Node();
                    if (nextFinal) {
                        next.isFinal = true;
                        next.words = 1;
                    }
                    nodes.remove(currentNode);
                    currentNode.setChild(c, next);
                }
                currentNode.words++;
                currentNode = next;
                trail.add(currentNode);
            }
            currentNode.words++;
            currentNode.setChild(word.charAt(word.length() - 1), finalNode);

            // minimize step
            for (int i = trail.size() - 1; i > 0; i--) {
                Node node = trail.get(i);
                Node equivalent = nodes.get(node);
                if (equivalent != null) {
                    Node parent = trail.get(i - 1);
                    char c = word.charAt(i - 1);
                    nodes.remove(parent);
                    parent.setChild(c, equivalent);
                    trail.set(i, equivalent);
                } else {
                    break;
                }
            }

            for (Node node : trail) {
                nodes.remove(node);
                nodes.put(node, node);
            }

            return this;
        }

        public Builder addAll(Collection<String> collection) {
            collection.stream().forEach(this::addWord);
            return this;
        }

        public DirectedAcyclicWordGraph build() {
            return DirectedAcyclicWordGraph.this;
        }
    }

    @Override
    public String toString() {
        Set<Node> all = new HashSet<>();
        Deque<Node> search = new ArrayDeque<>();
        Node node;
        while ((node = search.pollFirst()) != null) {
            all.add(node);
            for (Node child : node.children) {
                if (!all.contains(child)) {
                    search.add(child);
                }
            }
        }

        StringJoiner stringJoiner = new StringJoiner(", ");
        all.stream().forEach(n -> stringJoiner.add(n.toString()));
        return stringJoiner.toString();
    }

    class NodeIterator implements Iterator<Node> {
        private final StringBuilder word = new StringBuilder();

        private final LinkedList<Node> chain = new LinkedList<>();

        {
            chain.add(graph);
        }

        @Nullable
        private Node unwound = null;

        Node currentNode() {
            return chain.getLast();
        }

        void search() {
            while (chain.size() > 0) {
                for (int i = 0; i < currentNode().children.length; i++) {
                    Node child = currentNode().children[i];
                    if (unwound != null) {
                        if (child == unwound) {
                            unwound = null;
                        }
                        continue;
                    }
                    if (child != null) {
                        chain.add(child);
                        word.append(characterSet.forIndex(i));
                        return;
                    }
                }
                int length = word.length();
                if (length != 0) {
                    word.deleteCharAt(length - 1);
                }
                unwound = chain.removeLast();
            }
        }

        @Override
        public boolean hasNext() {
            return !chain.isEmpty();
        }

        @Override
        public Node next() {
            Node next = chain.getLast();
            search();
            return next;
        }

        String peekNextString() {
            return word.toString();
        }

        boolean nextIsFinal() {
            return currentNode().isFinal();
        }
    }

    public Iterator<String> iterator() {
        return new DAWGIterator();
    }

    class DAWGIterator implements Iterator<String> {
        @Nullable
        private String next;

        private NodeIterator nodeIterator = new NodeIterator();

        {
            findNext();
        }

        void findNext() {
            while (nodeIterator.hasNext()) {
                nodeIterator.next();
                if (nodeIterator.hasNext()) {
                    if (nodeIterator.nextIsFinal()) {
                        next = nodeIterator.peekNextString();
                        break;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public String next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            String next = this.next;
            this.next = null;
            findNext();
            return next;
        }
    }
}
