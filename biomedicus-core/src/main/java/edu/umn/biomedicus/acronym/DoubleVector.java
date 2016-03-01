package edu.umn.biomedicus.acronym;

import java.util.Set;
import java.util.function.Function;

/**
 * Simple interface for vectors of floating-point numbers, which will most likely be sparse and hashed
 *
 * @since 1.5.0
 */
public interface DoubleVector {

    /**
     * Get an element from this vector
     *
     * @param index the index of the returned element
     * @return the value at that element (zero if absent)
     */
    double get(int index);

    /**
     * The magnitude/length of this vector
     *
     * @return magnitude
     */
    double magnitude();

    /**
     * Normalize this vector to unit length
     */
    void normVector();

    /**
     * adds another vector to this one
     *
     * @param v the vector to add (argument vector should not be changed)
     */
    void add(DoubleVector v);

    /**
     * sets this vector to the Hadamard/elementwise product with another vector
     *
     * @param v the vector to multiply against this one (argument vector should not be changed)
     */
    void multiply(DoubleVector v);

    /**
     * Applies an operation to every element of this vector
     *
     * @param operation a function that takes and outputs Double (sqrt, e.g.)
     */
    void applyOperation(Function<Double, Double> operation);

    /**
     * Calculates this vector's dot product with another vector
     *
     * @param v the other vector
     * @return the dot product
     */
    double dot(DoubleVector v);

    /**
     * Get all non-zero keys of this vector
     *
     * @return a set of integer keys
     */
    Set<Integer> getKeySet();

}
