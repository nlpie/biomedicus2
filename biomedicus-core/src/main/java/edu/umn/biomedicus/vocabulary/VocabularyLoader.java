package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.terms.TermIndex;
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

    private final Path wordsPath;
    private final Path normsPath;

    @Inject
    public VocabularyLoader(BiomedicusConfiguration biomedicusConfiguration) {
        wordsPath = biomedicusConfiguration.resolveDataFile("vocabulary.wordIndex.path");
        normsPath = biomedicusConfiguration.resolveDataFile("vocabulary.normIndex.path");
    }

    @Override
    protected Vocabulary loadModel() throws BiomedicusException {
        try {
            LOGGER.info("Loading words into term index from path: {}", wordsPath);

            TermIndex wordIndex = new TermIndex();
            Files.lines(wordsPath).forEach(wordIndex::addTerm);

            LOGGER.info("Loading norms into term index from path: {}", normsPath);

            TermIndex normIndex = new TermIndex();
            Files.lines(normsPath).forEach(normIndex::addTerm);

            return new Vocabulary(wordIndex, normIndex);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
