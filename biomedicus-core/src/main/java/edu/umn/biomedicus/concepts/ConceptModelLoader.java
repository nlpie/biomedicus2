package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class ConceptModelLoader extends ModelLoader<ConceptModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path dictionaryPath;

    private final Path typesPath;

    @Inject
    ConceptModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        dictionaryPath = biomedicusConfiguration.resolveDataFile("concepts.dictionary.path");
        typesPath = biomedicusConfiguration.resolveDataFile("concepts.types.path");
    }

    @Override
    protected ConceptModel loadModel() {
        try (InputStream dictionaryIS = Files.newInputStream(dictionaryPath);
             InputStream typesIS = Files.newInputStream(typesPath)) {
            Yaml yaml = new Yaml(new SafeConstructor());

            LOGGER.info("Loading concepts dictionary: {}", dictionaryPath);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> conceptDictionary = (Map<String, List<String>>) yaml.load(dictionaryIS);

            LOGGER.info("Loading concept types: {}", typesPath);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> cuiToTuis = (Map<String, List<String>>) yaml.load(typesIS);

            return new ConceptModel(cuiToTuis, conceptDictionary);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load model files", e);
        }
    }
}
