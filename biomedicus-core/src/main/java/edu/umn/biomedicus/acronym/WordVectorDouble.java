package edu.umn.biomedicus.acronym;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * An implementation of sparse vectors of floating-point values (need not be word-based, really)
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class WordVectorDouble implements Serializable, DoubleVector {

    // Hash of values in this vector
    private Map<Integer, Double> vector;

    /**
     * Constructor, which needs the values already set
     *
     * @param vector a map between Integers and Doubles to create a new vector from
     */
    public WordVectorDouble(Map<Integer, Double> vector) {
        this.vector = vector;
    }

    public double magnitude() {
        double sqsum = 0;
        for (double x : vector.values()) {
            sqsum += Math.pow(x, 2);
        }
        return Math.pow(sqsum, 0.5);
    }

    // Normalize this vector
    public void normVector() {
        double mag = (double) magnitude();
        for (Map.Entry<Integer, Double> e : vector.entrySet()) {
            e.setValue(e.getValue() / mag);
        }
    }

    // will add another vector onto this one
    public void add(DoubleVector v) {
        for (int i : v.getKeySet()) {
            vector.put(i, this.get(i) + v.get(i));
        }
    }

    // sets this vector to the Hadamard/elementwise product with the argument vector
    public void multiply(DoubleVector v) {
        for (int k : getKeySet()) {
            if (v.get(k) == 0) {
                vector.remove(k);
            } else {
                vector.put(k, vector.get(k) * v.get(k));
            }
        }
    }

    // Applies an operation to every element of this vector
    public void applyOperation(Function<Double, Double> operation) {
        for (Map.Entry<Integer, Double> e : vector.entrySet()) {
            e.setValue(operation.apply(e.getValue()));
        }
    }

    /**
     * Return the dot product of this vector with another
     * Note that this operation is more efficient if you call it on the sparser vector
     *
     * @param v another vector
     * @return their dot product
     */
    public double dot(DoubleVector v) {
        double sum = 0;
        for (int i : vector.keySet()) {
            sum += vector.get(i) * v.get(i);
        }
        return sum;
    }

    /**
     * Getters: get the key set, or get an element
     */

    public Set<Integer> getKeySet() {
        return new HashSet<>(vector.keySet());
    }

    public double get(int i) {
        if (vector.containsKey(i))
            return vector.get(i);
        else
            return 0;
    }

}
