package edu.umn.biomedicus.common.collect;

/**
 *
 */
public interface Metric<T> {
    int compute(T first, T second);
}
