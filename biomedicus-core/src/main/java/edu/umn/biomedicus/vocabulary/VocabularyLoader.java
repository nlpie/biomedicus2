package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.terms.DAWGTermIndex;
import edu.umn.biomedicus.common.terms.DirectedAcyclicWordGraph;
import edu.umn.biomedicus.common.terms.MappedCharacterSet;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class VocabularyLoader extends DataLoader<Vocabulary> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Path characterSet;
    private final Path wordsPath;
    private final Path normsPath;

    @Inject
    public VocabularyLoader(BiomedicusConfiguration biomedicusConfiguration) {
        characterSet = biomedicusConfiguration.resolveDataFile("vocabulary.characters.path");
        wordsPath = biomedicusConfiguration.resolveDataFile("vocabulary.wordIndex.path");
        normsPath = biomedicusConfiguration.resolveDataFile("vocabulary.normIndex.path");
    }

    @Override
    protected Vocabulary loadModel() throws BiomedicusException {
        try {
            int[] chars = Files.lines(characterSet).flatMapToInt(String::chars).distinct().toArray();
            MappedCharacterSet.Builder builder = MappedCharacterSet.builder();
            for (int aChar : chars) {
                builder.add((char) aChar);
            }

            MappedCharacterSet mappedCharacterSet = builder.build();

            LOGGER.info("Loading words into term index from path: {}", wordsPath);

            DirectedAcyclicWordGraph.Builder wordsBuilder = DirectedAcyclicWordGraph.builder(mappedCharacterSet);
            Files.lines(wordsPath).forEach(wordsBuilder::addWord);
            DirectedAcyclicWordGraph wordGraph = wordsBuilder.build();
            DAWGTermIndex wordIndex = new DAWGTermIndex(wordGraph);

            LOGGER.info("Loading norms into term index from path: {}", normsPath);

            DirectedAcyclicWordGraph.Builder normsBuilder = DirectedAcyclicWordGraph.builder(mappedCharacterSet);
            Files.lines(normsPath).forEach(normsBuilder::addWord);
            DirectedAcyclicWordGraph normGraph = normsBuilder.build();
            DAWGTermIndex normIndex = new DAWGTermIndex(normGraph);

            return new Vocabulary(wordIndex, normIndex);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }

    }
}
