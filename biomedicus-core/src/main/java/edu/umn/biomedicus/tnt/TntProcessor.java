package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 *
 */
public class TntProcessor implements DocumentProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Document document;

    private final TntPosTagger tntPosTagger;

    @Inject
    public TntProcessor(Document document, TntModel tntModel, @Setting("tnt.beam.threshold") Double beamThreshold) {
        this.document = document;
        this.tntPosTagger = new TntPosTagger(tntModel, beamThreshold);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Tagging tokens in document.");
        for (Sentence sentence : document.getSentences()) {
            tntPosTagger.tagSentence(sentence);
        }
    }
}
