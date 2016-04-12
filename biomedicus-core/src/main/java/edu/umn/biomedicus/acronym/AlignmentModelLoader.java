package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class AlignmentModelLoader extends DataLoader<AlignmentModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path modelPath;

    @Inject
    public AlignmentModelLoader(@Named("acronym.alignmentModel.path") Path modelPath) {
        this.modelPath = modelPath;
    }

    @Override
    protected AlignmentModel loadModel() throws BiomedicusException {
        LOGGER.info("Loading acronym alignment model: {}", modelPath);

        Yaml yaml = YamlSerialization.createYaml();

        try {
            return (AlignmentModel) yaml.load(Files.newBufferedReader(modelPath));
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
