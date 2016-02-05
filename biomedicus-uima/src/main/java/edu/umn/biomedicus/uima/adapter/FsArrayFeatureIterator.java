package edu.umn.biomedicus.uima.adapter;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;
import java.util.function.Function;

/**
 *
 */
class FsArrayFeatureIterator<T extends Annotation, U> implements Iterator<U> {
    private final Function<Integer, T> getter;
    private final Function<T, U> adapter;
    private final int size;

    private int index = 0;

    public FsArrayFeatureIterator(Function<Integer, T> getter, Function<T, U> adapter, int size) {
        this.getter = getter;
        this.adapter = adapter;
        this.size = size;
    }

    @Override
    public boolean hasNext() {
        return index < size;
    }

    @Override
    public U next() {
        T annotation = getter.apply(index);
        index++;
        return adapter.apply(annotation);
    }
}
