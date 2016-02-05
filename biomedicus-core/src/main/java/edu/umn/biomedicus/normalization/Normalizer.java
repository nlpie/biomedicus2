package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Token;

import javax.inject.Inject;

/**
 *
 */
public class Normalizer implements DocumentProcessor {
    private final NormalizerModel normalizerModel;

    private final Document document;

    @Inject
    Normalizer(NormalizerModel normalizerModel, Document document) {
        this.normalizerModel = normalizerModel;
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Token token : document.getTokens()) {
            normalizerModel.normalize(token);
        }
    }
}
