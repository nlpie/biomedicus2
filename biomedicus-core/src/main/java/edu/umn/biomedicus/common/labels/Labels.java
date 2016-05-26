package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;

import java.util.Optional;

public interface Labels<T> {
    Iterable<Label<T>> all();

    Iterable<Label<T>> inSpan(Span span);

    Iterable<Label<T>> matchingSpan(Span span);

    Optional<Label<T>> oneMatchingSpan(Span span);
}
