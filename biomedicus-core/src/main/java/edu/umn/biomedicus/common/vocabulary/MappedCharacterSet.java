package edu.umn.biomedicus.common.vocabulary;

import edu.umn.biomedicus.common.collect.RangeMap;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 */
public class MappedCharacterSet implements CharacterSet {
    private final RangeMap rangeMap;
    private final Set<Character> mask;

    public MappedCharacterSet(RangeMap rangeMap) {
        this.rangeMap = rangeMap;
        mask = Collections.emptySet();
    }

    public MappedCharacterSet(RangeMap rangeMap, Set<Character> mask) {
        this.rangeMap = rangeMap;
        this.mask = mask;
    }

    @Override
    public int size() {
        return rangeMap.size();
    }

    @Override
    public int indexOf(Character character) {
        return mask.contains(character) ? -1 : rangeMap.map(character);
    }

    @Override
    public Character forIndex(int index) {
        int i = rangeMap.reverseMap(index);
        if (i == -1) {
            throw new IndexOutOfBoundsException();
        }
        char c = (char) i;
        if (mask.contains(c)) {
            throw new IndexOutOfBoundsException();
        }
        return c;
    }

    @Override
    public CharacterSet maskCharacters(CharSequence charSequence) {
        Set<Character> chars = charSequence.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        return new MappedCharacterSet(rangeMap, chars);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RangeMap.Builder builder = RangeMap.builder();

        private Builder() {

        }

        public Builder add(char c) {
            builder.add(c);
            return this;
        }

        public Builder addAll(CharSequence charSequence) {
            charSequence.chars().forEach(builder::add);
            return this;
        }

        public MappedCharacterSet build() {
            return new MappedCharacterSet(builder.build());
        }
    }
}
