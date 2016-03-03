package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class TntModelLoader extends ModelLoader<TntModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path trigram;

    private final Path wordModels;

    @Inject
    public TntModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        trigram = biomedicusConfiguration.resolveDataFile("tnt.trigram.path");
        wordModels = biomedicusConfiguration.resolveDataFile("tnt.word.path");
    }

    @Override
    protected TntModel loadModel() throws BiomedicusException {
        Yaml yaml = new Yaml(new PartOfSpeechConstructor(), new PartOfSpeechRepresenter());

        try {
            LOGGER.info("Loading TnT trigram model: {}", trigram);
            @SuppressWarnings("unchecked")
            Map<String, Object> store = (Map<String, Object>) yaml.load(Files.newInputStream(trigram));
            PosCapTrigramModel posCapTrigramModel = PosCapTrigramModel.createFromStore(store);

            List<FilteredAdaptedWordProbabilityModel> filteredAdaptedWordProbabilities = new ArrayList<>();
            Files.walkFileTree(wordModels, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    LOGGER.info("Loading TnT word model #{}: {}", filteredAdaptedWordProbabilities.size() + 1, file);
                    if (file.getFileName().toString().endsWith(".yml")) {
                        FilteredAdaptedWordProbabilityModel filteredAdaptedWordProbabilityModel = (FilteredAdaptedWordProbabilityModel) yaml.load(Files.newInputStream(file));
                        filteredAdaptedWordProbabilities.add(filteredAdaptedWordProbabilityModel);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            Collections.sort(filteredAdaptedWordProbabilities, (m1, m2) -> Integer.compare(m1.getPriority(), m2.getPriority()));

            return new TntModel(posCapTrigramModel, filteredAdaptedWordProbabilities);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
