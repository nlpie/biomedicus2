package edu.umn.biomedicus.opennlp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import edu.umn.biomedicus.exc.BiomedicusException;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
class OpenNlpParserModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ParserModel parserModel;

    @Inject
    OpenNlpParserModel(@Named("opennlp.parser.model.path") Path path) throws BiomedicusException {
        LOGGER.info("Loading OpenNLP parser model: {}", path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            parserModel = new ParserModel(inputStream);
        } catch (IOException e) {
            throw new BiomedicusException("Failed to load OpenNLP parser model: {}", e);
        }
    }

    Parser createParser() {
        return new ParserNoTagging(parserModel);
    }
}
