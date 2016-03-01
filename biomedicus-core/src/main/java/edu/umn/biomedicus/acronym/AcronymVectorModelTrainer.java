package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

/**
 * Trains a vector space model for acronym detection
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class AcronymVectorModelTrainer implements AcronymModelTrainer {

    private static final Logger LOGGER = LogManager.getLogger(AcronymVectorModelTrainer.class);
    // Number of tokens to look back/ahead when calculating word vectors
    private final static double DEFAULT_MAX_DIST = 9;
    // How counts will be transformed (default is sqrt to flatten out the vectors a bit)
    private static Function<Double, Double> transformCounts = Math::sqrt;
    // These are loaded from text files
    // expansionMap works the same as in AcronymVectorModel
    // uniqueIdMap maps unique identifying strings of the acronym long forms (as appear in the preprocessed training
    // text file) to their English forms
    private final Map<String, List<String>> expansionMap;
    private final Map<String, String> uniqueIdMap;
    private AlignmentModel alignmentModel;
    private double maxDist = DEFAULT_MAX_DIST;
    private VectorSpaceDouble vectorSpace = new VectorSpaceDouble(maxDist);
    // Will map senses to their centroid context vectors
    private Map<String, DoubleVector> senseMap = new HashMap<>();

    /**
     * Initializes the acronym trainer. Needs paths to two or three text files:
     *
     * @param expansionMapFile which maps short forms to long forms
     * @param uniqueIdMapFile  which maps unique identifying strings to long forms
     * @param longformsFile    which contains a list of long forms needed for the alignment model (unknown abbrs)
     * @throws IOException
     */
    public AcronymVectorModelTrainer(String expansionMapFile, String uniqueIdMapFile, @Nullable String longformsFile) throws IOException {
        Map<String, List<String>> expansionMapBuilder = new HashMap<>();
        Map<String, String> uniqueIdMapBuilder = new HashMap<>();

        LOGGER.info("Loading data files to initialize the acronym model trainer");

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(expansionMapFile);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine;
        while ((nextLine = fileReader.readLine()) != null) {
            List<String> fields = Arrays.asList(nextLine.split("\\|"));
            if (fields.size() >= 1) {
                String acronym = AcronymModel.standardFormString(fields.get(0));
                List<String> senses = new ArrayList<>(fields.subList(1, fields.size()));
                expansionMapBuilder.put(acronym, senses);
            }
        }

        InputStream inputStreamB = Thread.currentThread().getContextClassLoader().getResourceAsStream(uniqueIdMapFile);
        BufferedReader fileReaderB = new BufferedReader(new InputStreamReader(inputStreamB));
        while ((nextLine = fileReaderB.readLine()) != null) {
            String[] fields = nextLine.split("\\|");
            uniqueIdMapBuilder.put(fields[0], fields[1]);
        }

        expansionMap = expansionMapBuilder;
        uniqueIdMap = uniqueIdMapBuilder;
        if (longformsFile != null)
            alignmentModel = new AlignmentModel(longformsFile);
    }

    /**
     * Constructor if not providing terms for the AlignmentModel
     */
    public AcronymVectorModelTrainer(String expansionMapFile, String uniqueIdMapFile) throws IOException {
        this(expansionMapFile, uniqueIdMapFile, null);
    }

    /**
     * Call this after all documents have been added using addDocumentToModel(Document)
     * This will finalize the vectors and put them all into a knew AcronymVectorModel, which can be used or serialized
     *
     * @return a finalized AcronymVectorModel
     */
    public AcronymVectorModel getModel() {
        vectorSpace.finishTraining();

        // Apply some final operations to the model--most critically, normalization
        for (DoubleVector vector : senseMap.values()) {
            vector.applyOperation(transformCounts);
            vector.multiply(vectorSpace.getIdf());
            vector.normVector();
            // Multiply the idf post-normalization: this is equivalent to applying the idf to test vectors
            vector.multiply(vectorSpace.getIdf());
        }

        return new AcronymVectorModel(vectorSpace, senseMap, expansionMap, alignmentModel);
    }

    /**
     * Adds a document to the model, which should have been initialized already
     *
     * @param document a tokenized document
     */
    public void addDocumentToModel(Document document) {

        // Maximum number of words to look at (needn't look much farther than maxDist)
        int maxSize = (int) (maxDist * 1.5);

        // These lists will all be the same length
        // Tokens that are unique IDs, which will be used for training
        List<Token> tokensOfInterest = new ArrayList<>();
        // Chunks where our tokens of interest will be
        List<Integer> startPositions = new ArrayList<>();
        List<Integer> endPositions = new ArrayList<>();

        List<Token> allTokens = new ArrayList<>();

        // Our position in the document, for building the lists
        int i = 0;

        // Find tokens of interest in the document and their context; populate the above lists
        for (Token token : document.getTokens()) {
            allTokens.add(token);
            if (uniqueIdMap.containsKey(token.getText())) {
                tokensOfInterest.add(token);
                startPositions.add(i - maxSize);
                endPositions.add(i + maxSize + 1);
            }
            i++;
        }

        // Now go through to every token of interest, calculate a vector for it, and add it to the vectors already
        // found for that sense.
        i = 0;
        for (Token tokenOfInterest : tokensOfInterest) {
            String senseEnglish = uniqueIdMap.get(tokenOfInterest.getText());
            int start = startPositions.get(i);
            int end = endPositions.get(i);
            if (start < 0) start = 0;
            if (end > allTokens.size()) end = allTokens.size() - 1;

            DoubleVector calculatedVector = vectorSpace.vectorize(allTokens.subList(start, end), tokenOfInterest);

            if (senseMap.putIfAbsent(senseEnglish, calculatedVector) != null) {
                senseMap.get(senseEnglish).add(calculatedVector);
            }

            i++;
        }
    }

    /**
     * Counts will be transformed (after summing) by a function. The default is Math.sqrt
     *
     * @param function a different function to use (takes and returns double)
     */
    public void setTransformFunction(Function<Double, Double> function) {
        transformCounts = function;
    }

}
