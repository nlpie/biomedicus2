package edu.umn.biomedicus.common.collect;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 */
public class HashIndexMap<T> implements IndexMap<T> {
    private final HashMap<T, Integer> indexMap;

    private final HashMap<Integer, T> instanceMap;

    public HashIndexMap() {
        indexMap = new HashMap<>();
        instanceMap = new HashMap<>();
    }

    public HashIndexMap(Collection<? extends T> collection) {
        indexMap = new HashMap<>();
        instanceMap = new HashMap<>();
        collection.forEach(this::addItem);
    }

    @Override
    public int size() {
        return indexMap.size();
    }

    @Override
    public boolean contains(T item) {
        return indexMap.containsKey(item);
    }

    @Override
    public void addItem(T item) {
        if (!(indexMap.containsKey(item))) {
            int index = indexMap.size();
            indexMap.put(item, index);
            instanceMap.put(index, item);
        }
    }

    @Override
    public T forIndex(int index) {
        return instanceMap.get(index);
    }

    @Override
    public Integer indexOf(T item) {
        return indexMap.get(item);
    }
}
