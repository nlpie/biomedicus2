package edu.umn.biomedicus.common.vocabulary;

/**
 *
 */
public interface CharacterSet {
    int size();

    int getIndex(char c);

    char getCharacter(int index);
}
