package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 *
 */
public class Normalizer implements DocumentProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final NormalizerModel normalizerModel;

    private final Document document;

    @Inject
    Normalizer(NormalizerModel normalizerModel, Document document) {
        this.normalizerModel = normalizerModel;
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Normalizing tokens in a document.");
        for (Token token : document.getTokens()) {
            normalizerModel.normalize(token);
        }
    }
}
