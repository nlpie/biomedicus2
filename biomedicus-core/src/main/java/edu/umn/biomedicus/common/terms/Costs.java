package edu.umn.biomedicus.common.terms;

/**
 *
 */
public class Costs {
    public static final Costs LEVENSHTEIN = new Costs(0, 1, 1, 1);

    private final int match;

    private final int replace;

    private final int delete;

    private final int insert;

    public Costs(int match, int replace, int delete, int insert) {
        this.match = match;
        this.replace = replace;
        this.delete = delete;
        this.insert = insert;
    }

    public int getMatch() {
        return match;
    }

    public int getReplace() {
        return replace;
    }

    public int getDelete() {
        return delete;
    }

    public int getInsert() {
        return insert;
    }
}
