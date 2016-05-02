package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DocumentScoped
public class NormLabeler implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormLabeler.class);

    private final Document document;

    private final TermIndex wordIndex;

    @Inject
    public NormLabeler(Document document, Vocabulary vocabulary) {
        this.document = document;
        this.wordIndex = vocabulary.wordIndex();
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Labeling norm term index identifiers in a document.");
        for (Token token : document.getTokens()) {
            String normalForm = token.getNormalForm();
            token.setNormTerm(wordIndex.getIndexedTerm(normalForm));
        }
    }
}
