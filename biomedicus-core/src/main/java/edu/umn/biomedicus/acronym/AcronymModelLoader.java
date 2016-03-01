package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads the acronym model.
 *
 * @since 1.5.0
 */
@Singleton
public class AcronymModelLoader extends ModelLoader<AcronymModel> {

    private final Path path;

    @Inject
    public AcronymModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        path = biomedicusConfiguration.resolveDataFile("acronym.acronymModel.path");
    }

    @Override
    protected AcronymModel loadModel() throws BiomedicusException {
        try {
            return AcronymModel.loadFromSerialized(path.toString());
        } catch (IOException | ClassNotFoundException e) {
            throw new BiomedicusException(e);
        }
    }
}
