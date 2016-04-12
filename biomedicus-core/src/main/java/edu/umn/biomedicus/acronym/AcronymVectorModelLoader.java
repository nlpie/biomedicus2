package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class AcronymVectorModelLoader extends DataLoader<AcronymVectorModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Provider<AlignmentModel> alignmentModel;

    private final Path vectorSpacePath;

    private final Path senseMapPath;

    private final Path acronymExpansionsPath;

    private final boolean useAlignment;


    @Inject
    public AcronymVectorModelLoader(Provider<AlignmentModel> alignmentModel,
                                    @Named("acronym.useAlignment") Boolean useAlignment,
                                    @Named("acronym.vectorSpace.path") Path vectorSpacePath,
                                    @Named("acronym.senseMap.path") Path senseMapPath,
                                    @Named("acronym.acronymExpansions.path") Path acronymExpansionsPath) {
        this.alignmentModel = alignmentModel;
        this.useAlignment = useAlignment;
        this.vectorSpacePath = vectorSpacePath;
        this.senseMapPath = senseMapPath;
        this.acronymExpansionsPath = acronymExpansionsPath;
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
            Map<String, DoubleVector> senseMap = (Map<String, DoubleVector>) yaml.load(Files.newBufferedReader(senseMapPath));

            LOGGER.info("Loading acronym expansions: {}", acronymExpansionsPath);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> expansions = (Map<String, List<String>>) yaml.load(Files.newBufferedReader(acronymExpansionsPath));

            return new AcronymVectorModel(vectorSpaceDouble, senseMap, expansions, useAlignment ? alignmentModel.get() : null);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
