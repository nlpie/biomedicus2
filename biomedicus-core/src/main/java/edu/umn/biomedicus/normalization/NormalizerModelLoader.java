package edu.umn.biomedicus.normalization;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.model.tuples.WordPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 */
@Singleton
public class NormalizerModelLoader extends ModelLoader<NormalizerModel> {
    private final Logger LOGGER = LogManager.getLogger();

    private final Path lexiconFile;

    private final Path fallbackLexiconFile;

    @Inject
    public NormalizerModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        lexiconFile = biomedicusConfiguration.resolveDataFile("normalization.lexicon.path");
        fallbackLexiconFile = biomedicusConfiguration.resolveDataFile("normalization.fallback.path");
    }

    @Override
    protected NormalizerModel loadModel() {
        Yaml yaml = new Yaml();
        try {
            LOGGER.info("Loading normalization lexicon file: {}", lexiconFile);
            @SuppressWarnings("unchecked")
            Map<WordPos, String> lexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(lexiconFile));

            LOGGER.info("Loading normalization fallback lexicon file: {}", fallbackLexiconFile);
            @SuppressWarnings("unchecked")
            Map<WordPos, String> fallbackLexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(fallbackLexiconFile));

            return new NormalizerModel(lexicon, fallbackLexicon);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Normalizer model", e);
        }
    }
}
