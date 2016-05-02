package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DocumentScoped
public class MisspellingCorrector implements DocumentProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingCorrector.class);

    private final Document document;

    private final SpellingModel spellingModel;

    @Inject
    public MisspellingCorrector(Document document, SpellingModel spellingModel) {
        this.document = document;
        this.spellingModel = spellingModel;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Correcting any misspelled words in a document.");
        for (Sentence sentence : document.getSentences()) {
            String first = "<NONE>";
            String prev = "<NONE>";
            for (Token token : sentence.getTokens()) {
                String text = token.getText();
                if (Patterns.ALPHABETIC_WORD.matcher(text).matches()) {
                    return;
                }
                String word = text.toLowerCase();
                if (token.isMisspelled()) {
                    String suggested = spellingModel.suggestCorrection(word, Ngram.create(first, prev));
                    if (suggested != null) {
                        LOGGER.debug("Correcting word: {} with {}", word, suggested);
                        token.setCorrectSpelling(suggested);
                    }
                }
                first = prev;
                prev = word;
            }
        }
    }
}
