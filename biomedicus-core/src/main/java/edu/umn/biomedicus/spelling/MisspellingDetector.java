package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
@DocumentScoped
public class MisspellingDetector implements DocumentProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Document document;

    private final TermIndex wordIndex;

    @Inject
    public MisspellingDetector(Document document, Vocabulary vocabulary) {
        this.document = document;
        wordIndex = vocabulary.wordIndex();
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Finding any misspelled words in a document.");
        for (Token token : document.getTokens()) {
            String text = token.getText();
            if (Patterns.ALPHABETIC_WORD.matcher(text).matches() && !token.isCapitalized() && !wordIndex.contains(text.toLowerCase())) {
                token.setIsMisspelled(true);
            }
        }
    }
}
