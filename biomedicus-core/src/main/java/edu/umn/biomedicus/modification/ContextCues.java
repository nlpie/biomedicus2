package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;

import java.nio.file.Path;
import java.util.List;

public class ContextCues {
    private List<String> leftContextCues;
    private int leftContextScope;
    private List<String> rightContextCues;
    private int rightContextScope;
    private List<PartOfSpeech> scopeDelimitersPos;
    private List<String> scopeDelimitersTxt;


    public List<String> getLeftContextCues() {
        return leftContextCues;
    }

    public ContextCues setLeftContextCues(List<String> leftContextCues) {
        this.leftContextCues = leftContextCues;
        return this;
    }

    public List<String> getRightContextCues() {
        return rightContextCues;
    }

    public ContextCues setRightContextCues(List<String> rightContextCues) {
        this.rightContextCues = rightContextCues;
        return this;
    }

    public int getLeftContextScope() {
        return leftContextScope;
    }

    public ContextCues setLeftContextScope(int leftContextScope) {
        this.leftContextScope = leftContextScope;
        return this;
    }

    public int getRightContextScope() {
        return rightContextScope;
    }

    public ContextCues setRightContextScope(int rightContextScope) {
        this.rightContextScope = rightContextScope;
        return this;
    }

    public List<PartOfSpeech> getScopeDelimitersPos() {
        return scopeDelimitersPos;
    }

    public ContextCues setScopeDelimitersPos(List<PartOfSpeech> scopeDelimitersPos) {
        this.scopeDelimitersPos = scopeDelimitersPos;
        return this;
    }

    public List<String> getScopeDelimitersTxt() {
        return scopeDelimitersTxt;
    }

    public ContextCues setScopeDelimitersTxt(List<String> scopeDelimitersTxt) {
        this.scopeDelimitersTxt = scopeDelimitersTxt;
        return this;
    }

    public static ContextCues deserialize(Path path) {
        return null;
    }

    public void serialize(Path path) {

    }
}
