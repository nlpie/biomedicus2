package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class ConceptModelLoader extends DataLoader<ConceptModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path phrasesPath;

    private final Path normsPath;

    private final Path typesPath;

    @Inject
    ConceptModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        phrasesPath = biomedicusConfiguration.resolveDataFile("concepts.phrases.path");
        normsPath = biomedicusConfiguration.resolveDataFile("concepts.norms.path");
        typesPath = biomedicusConfiguration.resolveDataFile("concepts.types.path");
    }

    @Override
    protected ConceptModel loadModel() throws BiomedicusException {
        Yaml yaml = YamlSerialization.createYaml();

        try {
            LOGGER.info("Loading concepts phrases: {}", phrasesPath);
            @SuppressWarnings("unchecked")
            Map<String, List<CUI>> phraseDictionary = (Map<String, List<CUI>>) yaml.load(Files.newBufferedReader(phrasesPath));

            LOGGER.info("Loading concept norm vectors: {}", normsPath);
            @SuppressWarnings("unchecked")
            Map<List<String>, List<CUI>> normDictionary = (Map<List<String>, List<CUI>>) yaml.load(Files.newBufferedReader(normsPath));

            LOGGER.info("Loading CUI -> TUIs map: {}", typesPath);
            @SuppressWarnings("unchecked")
            Map<CUI, List<TUI>> cuiToTUIs = (Map<CUI, List<TUI>>) yaml.load(Files.newBufferedReader(typesPath));

            return new ConceptModel(cuiToTUIs, normDictionary, phraseDictionary);

        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
