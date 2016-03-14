package edu.umn.biomedicus.common.terms;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class DAWGMap implements Map<CharSequence, Integer> {
    private final DirectedAcyclicWordGraph directedAcyclicWordGraph;

    public DAWGMap(DirectedAcyclicWordGraph directedAcyclicWordGraph) {
        this.directedAcyclicWordGraph = directedAcyclicWordGraph;
    }

    @Override
    public int size() {
        return directedAcyclicWordGraph.size();
    }

    @Override
    public boolean isEmpty() {
        return directedAcyclicWordGraph.size() == 0;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return key instanceof CharSequence && directedAcyclicWordGraph.contains((CharSequence) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return value instanceof Integer && directedAcyclicWordGraph.size() > (Integer) value;
    }

    @Nullable
    @Override
    public Integer get(Object key) {
        if (!(key instanceof CharSequence)) {
            return null;
        }
        int i = directedAcyclicWordGraph.indexOf((CharSequence) key);
        return i == -1 ? null : i;
    }

    @Override
    public Integer put(CharSequence key, Integer value) {
        throw new UnsupportedOperationException("modification not allowed");
    }

    @Override
    public Integer remove(Object key) {
        throw new UnsupportedOperationException("modification not allowed");
    }

    @Override
    public void putAll(Map<? extends CharSequence, ? extends Integer> m) {
        throw new UnsupportedOperationException("modification not allowed");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not allowed");
    }

    @Override
    public Set<CharSequence> keySet() {
        Iterator<String> iterator = directedAcyclicWordGraph.iterator();
        Spliterator<String> spliterator = Spliterators.spliterator(iterator, directedAcyclicWordGraph.size(), Spliterator.SIZED | Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).collect(Collectors.toSet());
    }

    @Override
    public Collection<Integer> values() {
        return IntStream.range(0, directedAcyclicWordGraph.size() - 1)
                .mapToObj(Integer::new)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<CharSequence, Integer>> entrySet() {
        return IntStream.range(0, directedAcyclicWordGraph.size() - 1)
                .mapToObj(i ->  new AbstractMap.SimpleImmutableEntry<>((CharSequence) directedAcyclicWordGraph.forIndex(i), i))
                .collect(Collectors.toSet());
    }
}
