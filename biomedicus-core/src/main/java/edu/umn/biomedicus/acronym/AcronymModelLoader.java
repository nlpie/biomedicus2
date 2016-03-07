package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads the acronym model.
 *
 * @since 1.5.0
 */
@Singleton
public class AcronymModelLoader extends DataLoader<AcronymModel> {
    private final Logger LOGGER = LogManager.getLogger();

    private final Path path;

    @Inject
    public AcronymModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        path = biomedicusConfiguration.resolveDataFile("acronym.acronymModel.path");
    }

    @Override
    protected AcronymModel loadModel() throws BiomedicusException {
        LOGGER.info("Loading acronym model: {}", path);
        Yaml yaml = new Yaml();
        try {
            return (AcronymModel) yaml.load(Files.newBufferedReader(path));
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
