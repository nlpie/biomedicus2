package edu.umn.biomedicus.common.labels;

public interface Labeler<T> {
    ValueLabeler value(T value);
}
