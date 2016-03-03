package edu.umn.biomedicus.common.vocabulary;

import edu.umn.biomedicus.common.collect.IndexMapping;

/**
 *
 */
public interface CharacterSet extends IndexMapping<Character> {
    CharacterSet maskCharacters(CharSequence charSequence);
}
