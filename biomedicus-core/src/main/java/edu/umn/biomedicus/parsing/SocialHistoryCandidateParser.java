package edu.umn.biomedicus.parsing;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DocumentScoped
public class SocialHistoryCandidateParser implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocialHistoryCandidateParser.class);

    private final Document document;

    private final Parser candidateParser;

    @Inject
    public SocialHistoryCandidateParser(Document document,
                                        @ProcessorSetting("socialhistory.parser") Parser candidateParser) {
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
