package edu.umn.biomedicus.tnt;

import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class TntProcessorModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private TntPosTagger tntPosTagger;

    @Inject
    TntProcessorModel(BiomedicusConfiguration biomedicusConfiguration) {
        Path path = biomedicusConfiguration.resolveDataFile("tnt.model.path");
        LOGGER.info("Loading tnt model: {}", path);

        double beamThreshold = biomedicusConfiguration.getSettings().getAsDouble("tnt.beam.threshold");
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            TntModel tntModel = (TntModel) ois.readObject();
            tntPosTagger = new TntPosTagger(tntModel, beamThreshold);
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load TnT model", e);
        }
    }

    TntPosTagger getTntPosTagger() {
        return tntPosTagger;
    }
}
