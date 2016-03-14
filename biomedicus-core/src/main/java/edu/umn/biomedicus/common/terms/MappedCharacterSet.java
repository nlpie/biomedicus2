package edu.umn.biomedicus.common.terms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class MappedCharacterSet {
    private final int size;

    private final int[] indexes;
    private final char[] characters;

    public MappedCharacterSet(int size, int[] indexes, char[] characters) {
        this.size = size;
        this.indexes = indexes;
        this.characters = characters;
    }

    public int size() {
        return size;
    }

    public int indexOf(char character) {
        return indexes[character];
    }

    public char forIndex(int index) {
        return characters[index];
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<Character> characters = new HashSet<>();

        private Builder() {

        }

        public Builder add(char c) {
            characters.add(c);
            return this;
        }

        public Builder addAll(CharSequence charSequence) {
            charSequence.chars().forEach(c -> characters.add((char) c));
            return this;
        }

        public MappedCharacterSet build() {
            int[] chars = characters.stream().mapToInt(c -> (int) (char) c).sorted().toArray();
            char[] characters = new char[chars.length];
            int[] indexes = new int[chars[chars.length - 1] + 1];
            Arrays.fill(indexes, -1);
            for (int i = 0; i < chars.length; i++) {
                char c = (char) chars[i];
                characters[i] = c;
                indexes[c] = i;
            }
            return new MappedCharacterSet(chars.length, indexes, characters);
        }
    }
}
