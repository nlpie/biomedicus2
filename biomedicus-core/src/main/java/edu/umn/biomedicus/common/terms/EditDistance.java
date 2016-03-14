package edu.umn.biomedicus.common.terms;

/**
 *
 */
public interface EditDistance {
    /**
     * Computes the edit distance using a DP method, since we're not returning the traceback of operations, we don't
     * need to store anything more than two rows at a time.
     *
     * @return edit distance for transforming first word into the second.
     */
    int compute(CharSequence first, CharSequence second);
}
