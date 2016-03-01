package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @since 1.5.0
 */
@DocumentScoped
public class AcronymProcessor implements DocumentProcessor {
    /**
     *
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Document being analyzed.
     */
    private final Document document;

    /**
     * Acronym detector.
     */
    private final AcronymDetector acronymDetector;

    /**
     * Acronym expander.
     */
    private final AcronymExpander acronymExpander;

    @Inject
    public AcronymProcessor(Document document, AcronymDetector acronymDetector, AcronymExpander acronymExpander) {
        this.document = document;
        this.acronymDetector = acronymDetector;
        this.acronymExpander = acronymExpander;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Finding and expanding acronyms in document.");
        acronymDetector.detectAcronyms(document);
        acronymExpander.expandAcronyms(document);
    }
}
