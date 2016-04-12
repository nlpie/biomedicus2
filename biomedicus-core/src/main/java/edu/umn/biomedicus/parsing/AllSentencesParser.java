package edu.umn.biomedicus.parsing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class AllSentencesParser implements DocumentProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Document document;
    private final Parser parser;

    @Inject
    public AllSentencesParser(Document document, @Named("parser.implementation") Parser parser) {
        this.document = document;
        this.parser = parser;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Performing constituent parsing for a document.");
        for (Sentence sentence : document.getSentences()) {
            parser.parseSentence(sentence);
        }
    }
}
