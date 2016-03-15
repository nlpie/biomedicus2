package edu.umn.biomedicus.acronym;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.application.PostProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Trains a vector space model for acronym detection
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@ProvidedBy(AcronymVectorModelTrainerLoader.class)
public class AcronymVectorModelTrainer implements PostProcessor {

    private static final Pattern SPLITTER = Pattern.compile("\\|");

    private static final Pattern DE_ID = Pattern.compile("_%#.*#%_");

    private static final Logger LOGGER = LogManager.getLogger(AcronymVectorModelTrainer.class);

    // Number of tokens to look back/ahead when calculating word vectors
    private final static double DEFAULT_MAX_DIST = 9;
    // These are loaded from text files
    // expansionMap works the same as in AcronymVectorModel
    // uniqueIdMap maps unique identifying strings of the acronym long forms (as appear in the preprocessed training
    // text file) to their English forms
    private final Map<String, List<String>> expansionMap;
    private final Map<String, String> uniqueIdMap;
    private final AlignmentModel alignmentModel;
    private final Path outputDir;
    private double maxDist = DEFAULT_MAX_DIST;
    private final VectorSpaceDouble vectorSpace;
    // Will map senses to their centroid context vectors
    private Map<String, DoubleVector> senseMap = new HashMap<>();

    public AcronymVectorModelTrainer(Map<String, List<String>> expansionMap, Map<String, String> uniqueIdMap, AlignmentModel alignmentModel, Path outputDir) {
        this.expansionMap = expansionMap;
        this.uniqueIdMap = uniqueIdMap;
        this.alignmentModel = alignmentModel;
        vectorSpace = new VectorSpaceDouble();
        vectorSpace.setMaxDist(maxDist);
        this.outputDir = outputDir;
    }

    /**
     * Initializes the acronym trainer. Needs paths to two or three text files:
     *
     * @param expansionMapPath which maps short forms to long forms
     * @param uniqueIdMapPath  which maps unique identifying strings to long forms
     * @param longformsPath    which contains a list of long forms needed for the alignment model (unknown abbrs)
     * @param acronymModelPath
     * @throws IOException
     */
    public static AcronymVectorModelTrainer create(Path expansionMapPath,
                                                   Path uniqueIdMapPath,
                                                   @Nullable Path longformsPath,
                                                   Path acronymModelPath) throws IOException {
        LOGGER.info("Loading data files to initialize the acronym model trainer");

        Map<String, List<String>> expansionMap = Files.lines(expansionMapPath)
                .map(SPLITTER::split)
                .collect(Collectors.toMap(splits -> splits[0],
                        splits -> Arrays.asList(Arrays.copyOfRange(splits, 1, splits.length))));

        Map<String, String> uniqueIdMap = Files.lines(uniqueIdMapPath)
                .map(SPLITTER::split)
                .collect(Collectors.toMap(splits -> splits[0], splits -> splits[1]));

        AlignmentModel alignmentModel = longformsPath != null ? AlignmentModel.create(longformsPath) : null;

        return new AcronymVectorModelTrainer(expansionMap, uniqueIdMap, alignmentModel, acronymModelPath);
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
            vector.applyOperation(Math::sqrt);
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

        // Find tokens of interest in the document and their context; populate the above lists
        for (Token token : document.getTokens()) {
            String text = token.getText();
            if (!DE_ID.matcher(text).matches()) {
                allTokens.add(token);
                if (uniqueIdMap.containsKey(token.getText())) {
                    tokensOfInterest.add(token);
                    startPositions.add(allTokens.size() - 1 - maxSize);
                    endPositions.add(allTokens.size() + maxSize);
                }
            }
        }

        // Now go through to every token of interest, calculate a vector for it, and add it to the vectors already
        // found for that sense.
        int i = 0;
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

    @Override
    public void afterProcessing() throws BiomedicusException {
        AcronymVectorModel model = getModel();

        try {
            model.writeToDirectory(outputDir);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
