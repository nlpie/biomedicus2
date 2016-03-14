package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.application.ProcessorSettings;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class AcronymVectorModelTrainerLoader extends DataLoader<AcronymVectorModelTrainer> {

    private final Path expansionMapPath;

    private final Path uniqueIdMapPath;

    private final Path longformsPath;

    private final Path outputDir;

    @Inject
    public AcronymVectorModelTrainerLoader(ProcessorSettings processorSettings) {
        expansionMapPath = processorSettings.getSettings().getAsPath("expansionMap");
        uniqueIdMapPath = processorSettings.getSettings().getAsPath("uniqueIds");
        longformsPath = processorSettings.getSettings().getAsPath("longforms");
        outputDir = processorSettings.getSettings().getAsPath("outputDir");
    }

    @Override
    protected AcronymVectorModelTrainer loadModel() throws BiomedicusException {
        try {
            return AcronymVectorModelTrainer.create(expansionMapPath, uniqueIdMapPath, longformsPath, outputDir);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }

    public static void main(String[] args) throws BiomedicusException, IOException {
        Bootstrapper bootstrapper = new Bootstrapper();

        AcronymVectorModelTrainerLoader loader = bootstrapper.injector().getInstance(AcronymVectorModelTrainerLoader.class);

        AcronymVectorModelTrainer acronymVectorModelTrainer = loader.get();

        acronymVectorModelTrainer.afterProcessing();
    }
}
