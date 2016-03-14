package edu.umn.biomedicus.common.terms;

/**
 *
 */
public class DAWGNode {
    private int[] children;
    private int words;
    private boolean isFinal;

    public int[] getChildren() {
        return children;
    }

    public void setChildren(int[] children) {
        this.children = children;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public int getChild(int characterIndex) {
        return children[characterIndex];
    }

    public void setChild(int characterIndex, int node) {
        children[characterIndex] = node;
    }
}
