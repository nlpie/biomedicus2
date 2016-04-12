package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
@ProcessorScoped
@Singleton
public class AcronymVectorModelTrainerLoader extends DataLoader<AcronymVectorModelTrainer> {

    private final Path expansionMapPath;

    private final Path uniqueIdMapPath;

    private final Path longformsPath;

    private final Path outputDir;

    @Inject
    public AcronymVectorModelTrainerLoader(@ProcessorSetting("acronym.vector.trainer.expansionMap.path") Path expansionMapPath,
                                           @ProcessorSetting("acronym.vector.trainer.uniqueIdMap.path") Path uniqueIdMapPath,
                                           @ProcessorSetting("acronym.vector.trainer.longforms.path") Path longformsPath,
                                           @ProcessorSetting("acronym.vector.trainer.outputDir.path") Path outputDir) {
        this.expansionMapPath = expansionMapPath;
        this.uniqueIdMapPath = uniqueIdMapPath;
        this.longformsPath = longformsPath;
        this.outputDir = outputDir;
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
        AcronymVectorModelTrainerLoader loader = Bootstrapper.create().createClass(AcronymVectorModelTrainerLoader.class);

        AcronymVectorModelTrainer acronymVectorModelTrainer = loader.get();

        acronymVectorModelTrainer.allDocumentsProcessed();
    }
}
