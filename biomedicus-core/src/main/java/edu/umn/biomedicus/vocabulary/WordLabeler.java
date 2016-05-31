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
import edu.umn.biomedicus.common.text.WordIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DocumentScoped
public final class WordLabeler implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordLabeler.class);

    private final Document document;

    private final TermIndex wordIndex;

    private final Labeler<WordIndex> wordIndexLabeler;

    @Inject
    public WordLabeler(Document document, Vocabulary vocabulary, Labeler<WordIndex> wordIndexLabeler) {
        this.document = document;
        wordIndex = vocabulary.wordIndex();
        this.wordIndexLabeler = wordIndexLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Labeling word term index identifiers in a document.");
        for (Token token : document.getTokens()) {
            IndexedTerm indexedTerm = wordIndex.getIndexedTerm(token.getText());
            WordIndex wordIndex = new WordIndex(indexedTerm);
            wordIndexLabeler.value(wordIndex).label(token);
        }
    }
}
