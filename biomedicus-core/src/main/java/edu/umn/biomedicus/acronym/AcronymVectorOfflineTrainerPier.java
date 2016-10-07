///*
// * Copyright (c) 2016 Regents of the University of Minnesota.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package edu.umn.biomedicus.acronym;
//
//import edu.umn.biomedicus.exc.BiomedicusException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.*;
//import java.util.regex.Pattern;
//
///**
// * Trains an AcronymVectorModel based on pre-processed (space-tokenized) text.
// * Does not require the biomedicus tokenizer or other elements of the pipeline.
// * Can be used more efficiently than AcronymVectorModelTrainer on large corpora,
// * but misses out on features like spell checking and normalization
// *
// * Reads an AcronymExpansionsModel as previously generated by AcronymExpansionsBuilder
// * and finds co-occurrence vectors of all words and phrases corresponding to expansions in text
// *
// * Created by gpfinley on 5/25/16.
// */
//public class AcronymVectorOfflineTrainerPier {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymVectorOfflineTrainerPier.class);
//
//    final AcronymExpansionsModel aem;
//    private Map<String, Map<Integer, Double>> senseVectors;
//    VectorSpaceDouble vectorSpace;
//    private Map<String, Integer> wordFrequency;
//    private final Map<String, String> senseIds;
//
//    private int winSize;
//    private long totalDocs = 0;
//    // Only use these most common words
//    private final int nWords;
//    private static final int DEFAULT_N_WORDS = 100000;
//
//    // Stop counting words after so many bytes (should have a good idea of the top nWords by this point)
//    private final long maxBytesToCountWords = 5000000000L;
//    private long bytesWordCounted = 0;
//
//    // Will be used to calculate IDF
//    private long[] documentsPerTerm;
//
//    // Defines the point on the word graph where new words are no longer added
//    private final String TEXTBREAK = "\\W+";
//
//    public static void main(String[] args) throws BiomedicusException, IOException {
//        String expansionsFile = args[0];
//        String senseIdsFile = args[1];
//        String corpusPath = args[2];
//        String outDir = args.length > 3 ? args[3] : ".";
//        int nWords = args.length > 4 ? Integer.parseInt(args[4]) : DEFAULT_N_WORDS;
//        AcronymVectorOfflineTrainerPier trainer = new AcronymVectorOfflineTrainerPier(expansionsFile, senseIdsFile, nWords);
//        trainer.trainOnCorpus(corpusPath);
//        trainer.writeAcronymModel(outDir);
//    }
//
//    /**
//     * Initialize the trainer: read in possible acronym expansions
//     *
//     * @param expansionsFile a plaintext AcronymExpansionsModel (as created by AcronymExpansionsBuilder)
//     * @throws BiomedicusException
//     * @throws IOException
//     */
//    public AcronymVectorOfflineTrainerPier(String expansionsFile, String senseIdsFile, int nWords) throws BiomedicusException, IOException {
//        this.nWords = nWords;
//        // Get all possible acronym expansions and make vectors for each one
//        aem = new AcronymExpansionsModel.Loader(Paths.get(expansionsFile)).loadModel();
//        Set<String> allExpansions = new HashSet<>();
//        for (String acronym : aem.getAcronyms()) {
//            if(aem.getExpansions(acronym).size() > 1) {
//                allExpansions.addAll(aem.getExpansions(acronym));
//            }
//        }
//        LOGGER.info(allExpansions.size() + " possible acronym expansions/senses");
//        senseVectors = new HashMap<>();
//        for (String expansion : allExpansions) {
//            senseVectors.put(expansion, new HashMap<>());
//        }
//        senseIds = new HashMap<>();
//        BufferedReader reader = new BufferedReader(new FileReader(senseIdsFile));
//        String line;
//        while ((line=reader.readLine()) != null) {
//            String[] fields = line.split("\\|");
//            senseIds.put(fields[0].toLowerCase(), fields[1]);
//        }
//    }
//
//    /**
//     * Calculate word co-occurrence vectors from a corpus
//     * Will perform a prior pass on the corpus to get word counts if that has not already been done
//     * @param corpusPath path to a single file or directory (in which case all files will be visited recursively)
//     * @throws IOException
//     */
//    public void trainOnCorpus(String corpusPath) throws IOException {
//
//        // Get word counts from the training corpus if that wasn't done in a prior step
//        if(vectorSpace == null) {
//            getWordCounts(corpusPath);
//        }
//
//        // If this is the first time training on a corpus, initialize for that
//        if(documentsPerTerm == null) {
//            TreeSet<String> sortedWordFreq = new TreeSet<>(new ByValue(wordFrequency));
//            sortedWordFreq.addAll(wordFrequency.keySet());
//            Map<String, Integer> dictionary = new HashMap<>();
//            Iterator<String> iter = sortedWordFreq.descendingIterator();
//            for (int i = 0; i < nWords; i++) {
//                if (!iter.hasNext()) break;
//                String word = iter.next();
//                dictionary.put(word, i);
//            }
//            documentsPerTerm = new long[dictionary.size()];
//            vectorSpace.setDictionary(dictionary);
//        }
//
//        // generate co-occ vectors for all files in the corpus path
//        Files.walkFileTree(Paths.get(corpusPath), new FileVectorizer(true));
//
//    }
//
//    /**
//     * Get total word counts from a corpus before training co-occurrence vectors
//     * @param corpusPath path to a single file or directory (in which case all files will be visited recursively)
//     * @throws IOException
//     */
//    public void getWordCounts(String corpusPath) throws IOException {
//
//        vectorSpace = new VectorSpaceDouble();
//        wordFrequency = new HashMap<>();
//        winSize = (int) vectorSpace.getWindowSize();
//
//        Files.walkFileTree(Paths.get(corpusPath), new FileVectorizer(false));
//    }
//
//    /**
//     * Finalize vectors and write model
//     * Will apply square-rooting, normalization (the operations also performed by AcronymVectorModelTrainer)
//     *
//     * @param outFile file to serialize vectors to
//     * @throws IOException
//     */
//    public void writeAcronymModel(String outFile) throws IOException {
//        vectorSpace.finishTraining();
//
//        Map<Integer, Double> idfMap = new HashMap<>();
//        System.out.println(Arrays.toString(documentsPerTerm));
//        for(int i = 0; i < documentsPerTerm.length; i++) {
//            idfMap.put(i, Math.log(totalDocs / (documentsPerTerm[i] + 1.)));
//        }
//        SparseVector idfVector = new SparseVector(idfMap);
//        vectorSpace.setIdf(idfVector);
//
//        LOGGER.info("Creating vectors for senses");
//        // Create DoubleVectors out of the Integer->Double maps
//        Map<String, DoubleVector> senseDoubleVectors = new HashMap<>();
//        for (Map.Entry<String, Map<Integer, Double>> e : senseVectors.entrySet()) {
//            if (e.getValue().size() == 0) continue;
//            DoubleVector vector = new SparseVector(e.getValue());
//            vector.applyOperation(Math::sqrt);
//            vector.multiply(idfVector);
//            vector.normVector();
//            vector.multiply(idfVector);
//            senseDoubleVectors.put(e.getKey(), vector);
//        }
//
//        LOGGER.info(senseDoubleVectors.size() + " vectors total");
//
//        LOGGER.info("initializing acronym vector model");
//        AcronymVectorModel avm = new AcronymVectorModel(vectorSpace, senseDoubleVectors, aem, null);
//
//        vectorSpace = null;
//        senseVectors = null;
//
//        LOGGER.info("writing acronym vector model");
//        Path outPath = Paths.get(outFile);
//        avm.writeToDirectory(outPath);
//    }
//
//    private final static Pattern initialJunk = Pattern.compile("^\\W+");
//    private final static Pattern finalJunk = Pattern.compile("\\W+$");
//    private String[] tokenize(String orig) {
//        orig = initialJunk.matcher(orig).replaceFirst("");
//        orig = finalJunk.matcher(orig).replaceFirst("");
//        return orig.toLowerCase().split(TEXTBREAK);
//    }
//
//    /**
//     * Given a sense, its context, and its position in that context, add its surroundings to its context vector
//     * This step constitutes reading a 'document' for the purposes of IDF
//     *
//     * @param expansion the expansion string, which needs to match the expansions in the sense vector map
//     * @param words     context words
//     * @param startPos  array offset containing the beginning of the expansion word or phrase
//     * @param endPos    array offset one after the end of the expansion (always >= startPos + 1)
//     */
//    private void vectorizeForWord(String expansion, String[] words, int startPos, int endPos) {
//
//        Map<Integer, Double> vector = senseVectors.get(expansion);
//        if (vector == null) {
//            LOGGER.warn("Sense " + expansion + " not found in expansions file");
//            return;
//        }
//
//        int winStart = startPos - winSize;
//        int winEnd = endPos + winSize;
//        if(winStart < 0) winStart = 0;
//        if(winEnd > words.length) winEnd = words.length;
//
//        Set<Integer> wordsInThisDoc = new HashSet<>();
//
//        for(int i=winStart; i < winEnd; i++) {
//            // if we're in the expansion, skip to the end of it before adding words
//            if(i >= startPos && i < endPos) {
//                i = endPos;
//                continue;
//            }
//            Integer wordInt = vectorSpace.getDictionary().get(words[i]);
//            if(wordInt != null) {
//                wordsInThisDoc.add(wordInt);
//                Double oldVal = vector.putIfAbsent(wordInt, 1.0);
//                if(oldVal != null) {
//                    vector.put(wordInt, oldVal+1);
//                }
//            }
//        }
//        for(int wordInt : wordsInThisDoc) {
//            documentsPerTerm[wordInt]++;
//        }
//        totalDocs++;
//    }
//
//    /**
//     * Go through a chunk of text (as provided by file walker) and find all words that need to be vectorized
//     * Vectorize them using vectorizeForWord
//     * @param context
//     */
//    private void vectorizeChunk(String context) {
//        String[] words = tokenize(context);
//        for(int i=0; i<words.length; i++) {
//            if (words[i].length() == 0) continue;
//            if (!senseIds.containsKey(words[i])) continue;
//            vectorizeForWord(senseIds.get(words[i]), words, i, i);
//        }
//    }
//
//    /**
//     * Go through a chunk of text and count all the words in it
//     * @param context
//     */
//    private void countChunk(String context) {
//        String[] words = tokenize(context);
//        for(int i=0; i<words.length; i++) {
//            Integer oldVal = wordFrequency.putIfAbsent(words[i], 1);
//            if(oldVal != null) {
//                wordFrequency.put(words[i], oldVal+1);
//            }
//        }
//    }
//
//    /**
//     * For visiting multiple files under the same path, vectorizing or counting the words in each
//     */
//    private class FileVectorizer extends SimpleFileVisitor<Path> {
//
//        private boolean vectorizeNotCount;
//        FileVectorizer(boolean vectorizeNotCount) {
//            this.vectorizeNotCount = vectorizeNotCount;
//        }
//
//        @Override
//        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
//            if(file.getFileName().toString().startsWith(".")) {
//                return FileVisitResult.CONTINUE;
//            }
//            // Files that are larger than 100 MB should not be read all at once
//            if (file.toFile().length() < 100000000) {
//                Scanner scanner = new Scanner(file.toFile()).useDelimiter("\\Z");
//                String fileText = scanner.next();
//                scanner.close();
//                if(vectorizeNotCount) {
//                    vectorizeChunk(fileText);
//                } else {
//                    countChunk(fileText);
//                    bytesWordCounted += fileText.length();
//                    if (bytesWordCounted >= maxBytesToCountWords) {
//                        LOGGER.info("Done counting words.");
//                        return FileVisitResult.TERMINATE;
//                    }
//                }
//            } else {
//                // Make virtual files out of this file, splitting on whitespace every ~100 MB
//                BufferedReader reader = new BufferedReader(new FileReader(file.toFile()));
//                char[] chunk = new char[10000000];
//                long totalLength = 0;
//                while (reader.read(chunk) > 0) {
//                    String line = new String(chunk);
//                    while (true) {
//                        // This could be sped up--reading bytes one at a time is fantastically slow
//                        int nextByte = reader.read();
//                        char nextChar = (char) nextByte;
//                        if (nextByte < 0 || nextChar == ' ' || nextChar == '\t' || nextChar == '\n') break;
//                        line += (char) nextByte;
//                    }
//                    totalLength += line.length();
//                    if(vectorizeNotCount) {
//                        vectorizeChunk(line);
//                    } else {
//                        countChunk(line);
//                        LOGGER.info(wordFrequency.size() + " total words found");
//                        bytesWordCounted += line.length();
//                        if (bytesWordCounted >= maxBytesToCountWords) {
//                            LOGGER.info("Done counting words.");
//                            return FileVisitResult.TERMINATE;
//                        }
//                    }
//                    LOGGER.info(totalLength + " bytes of large file " + file + " processed");
//                }
//                reader.close();
//            }
//
//            LOGGER.info(file + " visited");
//
//            return FileVisitResult.CONTINUE;
//        }
//    }
//
//    /**
//     * General-use Comparator for sorting based on map values
//     * Be sure that the values are Comparable (will probably be Integer or Double)
//     * Created by gpfinley on 3/1/16.
//     */
//    public class ByValue implements Comparator {
//        private Map map;
//
//        public ByValue(Map map) {
//            this.map = map;
//        }
//
//        public int compare(Object o1, Object o2) {
//            Comparable o1val = (Comparable)map.get(o1);
//            Comparable o2val = (Comparable)map.get(o2);
//            int cmp = o1val.compareTo(o2val);
//            if(cmp == 0) {
//                if(o1 instanceof Comparable)
//                    return ((Comparable)o1).compareTo(o2);
//                else return 0;
//            }
//            return cmp;
//        }
//    }
//
//}
