package edu.umn.biomedicus.acronym;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts a serialized sense map to a different type that might be loaded in more efficiently
 *
 * Created by gpfinley on 10/6/16.
 */
public class TempConvertSenseMap {

    public static void main(String[] args) throws IOException {
        Path senseMapPath = Paths.get(args[0]);
        Path outPath = Paths.get(args[1]);

        saveNew(loadOld(senseMapPath), outPath);

        loadNew(outPath);
    }

    static Map<String, SparseVector> loadNew(Path senseMapPath) throws IOException {

        System.out.println("loading new...");
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(senseMapPath.toFile()));

        byte[] intBytes = new byte[4];
        Map<String, SparseVector> senseMap;
        try {
            String[] words = (String[]) stream.readObject();
            senseMap = new HashMap<>(words.length);
            for (String word : words) {
                stream.read(intBytes);
                int size = ByteBuffer.wrap(intBytes).getInt();
                Map<Integer, Double> vector = new HashMap<>(size);
                ByteBuffer buf = ByteBuffer.allocate(size*8);
                stream.readFully(buf.array());
                for (int i = 0; i < size; i++) {
                    int index = buf.getInt();
                    float val = buf.getFloat();
                    vector.put(index, (double) val);
                }
                senseMap.put(word, new SparseVector(vector));
            }
        } catch (ClassNotFoundException e) {
            throw new IOException();
        }

        stream.close();
        return senseMap;
    }

    static void saveNew(Map<String, SparseVector> senseMap, Path out) throws IOException {
        System.out.println("saving new...");
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(out.toFile()));

        String[] words = senseMap.keySet().toArray(new String[0]);
        stream.writeObject(words);

        for (String word : words) {
            Map<Integer, Double> vec = ((SparseVector)senseMap.get(word)).getVector();
            int size = vec.size();
            stream.write(ByteBuffer.allocate(4).putInt(size).array());
            ByteBuffer buf = ByteBuffer.allocate(size*8);
            for (Map.Entry<Integer, Double> e : vec.entrySet()) {
                buf.putInt(e.getKey());
                buf.putFloat((float)(double)e.getValue());
            }
            stream.write(buf.array());
        }
        stream.close();
    }

    static void saveOld(Map<String, SparseVector> senseMap, Path outFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile.toFile()));
        oos.writeObject(senseMap.size());
        for (Map.Entry<String, SparseVector> e : senseMap.entrySet()) {
            oos.writeObject(e.getKey());
            oos.writeObject(((SparseVector)e.getValue()).getVector());
        }
        oos.flush();
        oos.close();
    }

    static Map<String, SparseVector> loadOld(Path senseMapPath) throws IOException {
        System.out.println("loading old...");
        Map<String, SparseVector> map = new HashMap<>();
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(senseMapPath.toFile()));
            int size = (int) ois.readObject();
            for (int i = 0; i < size; i++) {
                String word = (String) ois.readObject();
                @SuppressWarnings("unchecked")
                Map<Integer, Double> readVector = (Map<Integer, Double>) ois.readObject();
                SparseVector vector = new SparseVector(readVector);
                map.put(word, vector);
            }
            ois.close();
        } catch (ClassNotFoundException e) {
            throw new IOException();
        }
        return map;
    }

}
