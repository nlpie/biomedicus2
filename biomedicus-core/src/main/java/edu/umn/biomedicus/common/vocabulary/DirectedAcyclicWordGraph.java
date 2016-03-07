package edu.umn.biomedicus.common.vocabulary;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 *
 */
public class DirectedAcyclicWordGraph extends AbstractCollection<CharSequence> implements Set<CharSequence> {
    private final CharacterSet characterSet;

    private final Node initialNode;

    private DirectedAcyclicWordGraph(CharacterSet characterSet, Node initialNode) {
        this.characterSet = characterSet;
        this.initialNode = initialNode;
    }

    public class Node {
        private Node[] children;
        private int words;
        private boolean isFinal;

        public Node() {
            children = new Node[characterSet.size()];
            words = 0;
            isFinal = false;
        }

        @Nullable
        public Node getChild(char c) {
            return children[characterSet.indexOf(c)];
        }

        public int indexUpTo(char c) {
            return Arrays.stream(children, 0, characterSet.indexOf(c)).mapToInt(child -> child.words).sum();
        }

        public Node[] getChildren() {
            return children;
        }

        public void setChildren(Node[] children) {
            this.children = children;
        }

        public int getWords() {
            return words;
        }

        public void setWords(int words) {
            this.words = words;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        public void clear() {
            children = new Node[characterSet.size()];
            words = 0;
            isFinal = false;
        }
    }

    public int getIndex(CharSequence word) {
        int index = 0;
        Node currentNode = initialNode;
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);
            Node child = currentNode.getChild(currentChar);
            if (child != null) {
                index += currentNode.indexUpTo(currentChar);
                currentNode = child;
                if (currentNode.isFinal) {
                    index++;
                }
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

    @Override
    public int size() {
        return initialNode.words;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof CharSequence)) {
            return false;
        }

        CharSequence charSequence = (CharSequence) o;
        Node currentNode = initialNode;
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

    @Override
    public Iterator<CharSequence> iterator() {
        return new Iterator<CharSequence>() {
            private List<Integer> path = new LinkedList<>();

            private StringBuilder word = new StringBuilder();

            private Node currentNode = initialNode;

            {
                advance();
            }

            @Override
            public boolean hasNext() {
                return currentNode.isFinal();
            }

            @Override
            public CharSequence next() {
                return word.toString();
            }

            public void advance() {

            }
        };
    }

    @Override
    public boolean add(CharSequence charSequence) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends CharSequence> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Modification is not supported");
    }

    @Override
    public void clear() {
        initialNode.clear();
    }

    @Override
    public Spliterator<CharSequence> spliterator() {
        return new Spliterator<CharSequence>() {
            @Override
            public boolean tryAdvance(Consumer<? super CharSequence> action) {
                return false;
            }

            @Override
            public Spliterator<CharSequence> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        };
    }
}
