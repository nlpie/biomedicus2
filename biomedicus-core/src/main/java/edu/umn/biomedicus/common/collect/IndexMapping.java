package edu.umn.biomedicus.common.collect;

/**
 * Maps an object type to array indexes. Used for objects that have a finite number of values.
 *
 * @since 1.5.0
 */
public interface IndexMapping<T> {
    int size();

    int indexOf(T t);

    T forIndex(int index);
}
