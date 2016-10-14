package edu.umn.biomedicus.acronym;

import java.util.Arrays;

/**
 * Created by gpfinley on 10/13/16.
 */
public class WordEmbedding {

    private float[] vector;

    public WordEmbedding(int dimensionality) {
        this(new float[dimensionality]);
    }

    public WordEmbedding(float[] vector) {
        this.vector = Arrays.copyOf(vector, vector.length);
    }

    public WordEmbedding(WordEmbedding orig) {
        vector = new float[orig.size()];
        for(int i=0; i < vector.length; i++) {
            vector[i] = (float)orig.get(i);
        }
    }

    public double get(int i) {
        return (double)vector[i];
    }

    public int size() {
        return vector.length;
    }

    public void normalize() {
        double mag = mag();
        if(mag == 0) return;
        for(int i=0; i<vector.length; i++) {
            vector[i] /= mag;
        }
    }

    public double mag() {
        double sqsum = 0;
        for(double x : vector) {
            sqsum += x * x;
        }
        return Math.sqrt(sqsum);
    }

    public double dot(WordEmbedding other) {
        double sum = 0;
        for(int i=0; i<vector.length; i++) {
            sum += vector[i] * other.get(i);
        }
        return sum;
    }

    public WordEmbedding sum(WordEmbedding other) {
        float[] sum = Arrays.copyOf(vector, vector.length);
        for(int i=0; i<vector.length; i++) {
            sum[i] += other.get(i);
        }
        return new WordEmbedding(sum);
    }

    public WordEmbedding hadamard(WordEmbedding other) {
        WordEmbedding prod = new WordEmbedding(this);
        for(int i=0; i<vector.length; i++) {
            prod.vector[i] *= other.vector[i];
        }
        return prod;
    }

    public WordEmbedding difference(WordEmbedding other) {
        float[] diff = Arrays.copyOf(vector, vector.length);
        for(int i=0; i<vector.length; i++) {
            diff[i] -= other.get(i);
        }
        return new WordEmbedding(diff);
    }

    public void add(WordEmbedding other) {
        for(int i=0; i<vector.length; i++) {
            vector[i] += other.get(i);
        }
    }

    public void add(double addend) {
        for(int i=0; i<vector.length; i++) {
            vector[i] += addend;
        }
    }

    public void subtract(WordEmbedding other) {
        for(int i=0; i<vector.length; i++) {
            vector[i] -= other.get(i);
        }
    }

    public void scalarMultiply(double s) {
        for(int i=0; i<vector.length; i++)
            vector[i] *= s;
    }

    public double cosSim(WordEmbedding other) {
        return dot(other) / mag() / other.mag();
    }

    public double euclidDist(WordEmbedding other) {
        double diff = 0;
        for(int i=0; i<vector.length; i++) {
            diff += Math.pow(vector[i] - other.get(i), 2);
        }
        return Math.sqrt(diff);
    }

    @Override
    public String toString() {
        return Arrays.toString(vector);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof WordEmbedding)) return false;
        for(int i=0; i<vector.length; i++) {
            if(vector[i] != ((WordEmbedding)other).get(i)) {
                return false;
            }
        }
        return true;
    }

}
