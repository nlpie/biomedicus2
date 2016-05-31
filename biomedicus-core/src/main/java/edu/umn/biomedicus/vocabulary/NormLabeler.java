package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.NormIndex;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DocumentScoped
public final class NormLabeler implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormLabeler.class);

    private final Document document;

    private final TermIndex wordIndex;

    private final Labeler<NormIndex> normIndexLabeler;

    @Inject
    public NormLabeler(Document document, Vocabulary vocabulary, Labeler<NormIndex> normIndexLabeler) {
        this.document = document;
        this.wordIndex = vocabulary.wordIndex();
        this.normIndexLabeler = normIndexLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Labeling norm term index identifiers in a document.");
        for (Token token : document.getTokens()) {
            String normalForm = token.getNormalForm();
            IndexedTerm indexedTerm = wordIndex.getIndexedTerm(normalForm);
            NormIndex normIndex = new NormIndex(indexedTerm);
            normIndexLabeler.value(normIndex).label(token);
        }
    }
}
