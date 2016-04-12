package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
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
public class TntModelLoader extends DataLoader<TntModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path trigram;

    private final Path wordModels;

    @Inject
    public TntModelLoader(@Setting("tnt.trigram.path") Path trigram, @Setting("tnt.word.path") Path wordModels) {
        this.trigram = trigram;
        this.wordModels = wordModels;
    }

    @Override
    protected TntModel loadModel() throws BiomedicusException {
        Yaml yaml = YamlSerialization.createYaml();

        try {
            LOGGER.info("Loading TnT trigram model: {}", trigram);
            @SuppressWarnings("unchecked")
            Map<String, Object> store = (Map<String, Object>) yaml.load(Files.newInputStream(trigram));
            PosCapTrigramModel posCapTrigramModel = PosCapTrigramModel.createFromStore(store);

            List<FilteredAdaptedWordProbabilityModel> filteredAdaptedWordProbabilities = new ArrayList<>();
            Files.walkFileTree(wordModels, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".yml")) {
                        LOGGER.info("Loading TnT word model #{}: {}", filteredAdaptedWordProbabilities.size() + 1, file);
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
