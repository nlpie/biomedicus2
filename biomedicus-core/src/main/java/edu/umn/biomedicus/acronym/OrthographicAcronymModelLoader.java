package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads the orthographic
 *
 * @since 1.5.0
 */
@Singleton
public class OrthographicAcronymModelLoader extends ModelLoader<OrthographicAcronymModel> {
    private final Path orthographicModel;

    @Inject
    OrthographicAcronymModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        orthographicModel = biomedicusConfiguration.resolveDataFile("acronym.orthographicModel.path");
    }

    @Override
    protected OrthographicAcronymModel loadModel() throws BiomedicusException {
        try {
            return OrthographicAcronymModel.loadFromSerialized(orthographicModel.toString());
        } catch (IOException | ClassNotFoundException e) {
            throw new BiomedicusException(e);
        }
    }
}
