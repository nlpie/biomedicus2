package edu.umn.biomedicus.parsing;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AllSentencesParser implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllSentencesParser.class);

    private final Document document;
    private final Parser parser;

    @Inject
    public AllSentencesParser(Document document, @Setting("parser.implementation") Parser parser) {
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
