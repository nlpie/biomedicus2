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
public class SocialHistoryCandidateParser implements DocumentProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Document document;

    private final Parser candidateParser;

    @Inject
    public SocialHistoryCandidateParser(Document document,
                                        @Named("parser.implementation") Parser candidateParser) {
        this.document = document;
        this.candidateParser = candidateParser;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Performing social history candidate constituent parsing for a document.");
        for (Sentence sentence : document.getSentences()) {
            if (sentence.isSocialHistoryCandidate() && sentence.getParseTree() == null) {
                candidateParser.parseSentence(sentence);
            }
        }
    }
}
