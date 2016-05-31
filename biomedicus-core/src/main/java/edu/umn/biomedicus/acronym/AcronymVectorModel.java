package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An implementation of an acronym model that uses word vectors and a cosine distance metric
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@ProvidedBy(AcronymVectorModel.Loader.class)
class AcronymVectorModel implements AcronymModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymVectorModel.class);

    /**
     * A vector space with a built dictionary to use at test time
     */
    private final VectorSpaceDouble vectorSpaceDouble;

    private final AcronymExpansionsModel acronymExpansionsModel;

    /**
     * Maps long forms to their trained word vectors
     */
    private final Map<String, DoubleVector> senseMap;

    /**
     * The alignment model will guess an acronym's full form based on its alignment if we don't know what it is.
     */
    private final AlignmentModel alignmentModel;

    /**
     * Constructor. Needs several things already made:
     *
     * @param vectorSpaceDouble the vector space (most importantly dictionary) used to build context vectors
     * @param senseMap          which maps between senses and their context vectors
     * @param alignmentModel    a model used for alignment of unknown acronyms
     */
    AcronymVectorModel(VectorSpaceDouble vectorSpaceDouble, Map<String, DoubleVector> senseMap, AcronymExpansionsModel acronymExpansionsModel, @Nullable AlignmentModel alignmentModel) {
        this.acronymExpansionsModel = acronymExpansionsModel;
        this.senseMap = senseMap;
        this.vectorSpaceDouble = vectorSpaceDouble;
        this.alignmentModel = alignmentModel;
    }

    /**
     * Will return a list of the possible senses for this acronym
     *
     * @param token a Token
     * @return a List of Strings of all possible senses
     */
    public Collection<String> getExpansions(Token token) {
        String acronym = Acronyms.standardForm(token);
        Collection<String> expansions = acronymExpansionsModel.getExpansions(acronym);
        if (expansions != null) {
            return expansions;
        }
        return Collections.emptyList();
    }

    /**
     * Does the model know about this acronym?
     *
     * @param token
     * @return
     */
    public boolean hasAcronym(Token token) {
        String acronym = Acronyms.standardForm(token);
        return acronymExpansionsModel.hasExpansions(acronym);
    }

    /**
     * Will return the model's best guess for the sense of this acronym
     *
     * @param context a list of tokens providing context for this acronym
     * @param token   the token of the acronym itself
     * @return
     */
    @Override
    public String findBestSense(List<Token> context, Token token) {

        // String to assign to unknown acronyms

        String acronym = Acronyms.standardForm(token);

        // If the model doesn't contain this acronym, make sure it doesn't contain an upper-case version of it

        Collection<String> senses = acronymExpansionsModel.getExpansions(acronym);
        if (senses == null) {
            senses = acronymExpansionsModel.getExpansions(acronym.toUpperCase());
        }
        if (senses == null) {
            senses = acronymExpansionsModel.getExpansions(acronym.toLowerCase());
        }
        if (senses == null && alignmentModel != null) {
            senses = alignmentModel.findBestLongforms(acronym);
        }
        if (senses == null || senses.size() == 0) {
            return Acronyms.UNKNOWN;
        }

        // If the acronym is unambiguous, our work is done
        if (senses.size() == 1) {
            return senses.iterator().next();
        }


        List<String> usableSenses = new ArrayList<>();
        // Be sure that there even are disambiguation vectors for senses
        for (String sense : senses) {
            if (senseMap.containsKey(sense)) {
                usableSenses.add(sense);
            }
        }

        // If no senses good for disambiguation were found, try the upper-case version
        if (usableSenses.size() == 0 && acronymExpansionsModel.hasExpansions(acronym.toUpperCase())) {
            for (String sense : senses) {
                if (senseMap.containsKey(sense)) {
                    usableSenses.add(sense);
                }
            }
        }

        // Should this just guess the first sense instead?
        if (usableSenses.size() == 0) {
            return Acronyms.UNKNOWN;
        }

        double best = -Double.MAX_VALUE;
        String winner = Acronyms.UNKNOWN;

        DoubleVector vector = vectorSpaceDouble.vectorize(context, token);
        vector.multiply(vectorSpaceDouble.getIdf());

        // Loop through all possible senses for this acronym
        for (String sense : usableSenses) {
            DoubleVector compVec = senseMap.get(sense);
            double score = vector.dot(compVec);
            if (score > best) {
                best = score;
                winner = sense;
            }
        }
        return winner;
    }

    /**
     * Remove a single word from the model
     *
     * @param word the word to remove
     */
    public void removeWord(String word) {
        vectorSpaceDouble.removeWord(word);
    }

    /**
     * Remove all words from the model except those given
     *
     * @param wordsToRemove the set of words to keep
     */
    public void removeWordsExcept(Set<String> wordsToRemove) {
        vectorSpaceDouble.removeWordsExcept(wordsToRemove);
    }

    void writeToDirectory(Path outputDir) throws IOException {
        Yaml yaml = YamlSerialization.createYaml();

        yaml.dump(alignmentModel, Files.newBufferedWriter(outputDir.resolve("alignment.yml")));
        yaml.dump(vectorSpaceDouble, Files.newBufferedWriter(outputDir.resolve("vectorSpace.yml")));
        serializeSenseMap(outputDir.resolve("senseMap.ser"));
    }

    /**
     * Binary serialization for the senseMap, which gets too big for yaml (and isn't very human readable anyway)
     * Serializes built-in java classes
     * @param outFile output file to serialize to
     * @throws IOException
     */
    private void serializeSenseMap(Path outFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile.toFile()));
        oos.writeObject(senseMap.size());
        for(Map.Entry<String, DoubleVector> e : senseMap.entrySet()) {
            oos.writeObject(e.getKey());
            oos.writeObject(e.getValue().getVector());
        }
        oos.flush();
        oos.close();
    }

    private static Map<String, DoubleVector> deserializeSenseMap(Path senseMapPath) throws IOException {
        Map<String, DoubleVector> map = new HashMap<>();
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(senseMapPath.toFile()));
            int size = (int) ois.readObject();
            for(int i=0; i<size; i++) {
                String word = (String) ois.readObject();
                DoubleVector vector = new WordVectorDouble();
                vector.setVector((Map<Integer, Double>) ois.readObject());
                map.put(word, vector);
            }
        } catch(ClassNotFoundException e) {
            throw new IOException();
        }
        return map;
    }

    /**
     *
     */
    @Singleton
    static class Loader extends DataLoader<AcronymVectorModel> {
        private final Provider<AlignmentModel> alignmentModel;

        private final Path vectorSpacePath;

        private final Path senseMapPath;

        private final boolean useAlignment;

        private final AcronymExpansionsModel expansionsModel;

        @Inject
        public Loader(Provider<AlignmentModel> alignmentModel,
                      @Setting("acronym.useAlignment") Boolean useAlignment,
                      @Setting("acronym.vector.model.path") Path vectorSpacePath,
                      @Setting("acronym.senseMap.path") Path senseMapPath,
                      AcronymExpansionsModel expansionsModel) {
            this.alignmentModel = alignmentModel;
            this.useAlignment = useAlignment;
            this.vectorSpacePath = vectorSpacePath;
            this.senseMapPath = senseMapPath;
            this.expansionsModel = expansionsModel;
        }

        @Override
        protected AcronymVectorModel loadModel() throws BiomedicusException {

            Yaml yaml = YamlSerialization.createYaml();

            try {
                LOGGER.info("Loading acronym vector space: {}", vectorSpacePath);
                @SuppressWarnings("unchecked")
                VectorSpaceDouble vectorSpaceDouble = (VectorSpaceDouble) yaml.load(Files.newBufferedReader(vectorSpacePath));

                LOGGER.info("Loading acronym sense map: {}", senseMapPath);
                @SuppressWarnings("unchecked")
                Map<String, DoubleVector> senseMap = deserializeSenseMap(senseMapPath);

                return new AcronymVectorModel(vectorSpaceDouble, senseMap, expansionsModel, useAlignment ? alignmentModel.get() : null);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
