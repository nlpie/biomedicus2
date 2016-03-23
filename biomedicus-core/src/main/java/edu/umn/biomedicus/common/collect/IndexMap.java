package edu.umn.biomedicus.common.collect;

/**
 *
 */
public interface IndexMap<T> {
    int size();

    boolean contains(T item);

    void addItem(T item);

    T forIndex(int index);

    Integer indexOf(T item);
}
