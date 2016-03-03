package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * Loads the acronym model.
 *
 * @since 1.5.0
 */
@Singleton
public class AcronymModelLoader extends ModelLoader<AcronymModel> {
    private final Logger LOGGER = LogManager.getLogger();

    private final Path path;

    @Inject
    public AcronymModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        path = biomedicusConfiguration.resolveDataFile("acronym.acronymModel.path");
    }

    @Override
    protected AcronymModel loadModel() throws BiomedicusException {
        LOGGER.info("Loading acronym model: {}", path);
        try (InputStream in = Files.newInputStream(path)) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(gzipInputStream);
            return (AcronymModel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new BiomedicusException(e);
        }
    }
}
