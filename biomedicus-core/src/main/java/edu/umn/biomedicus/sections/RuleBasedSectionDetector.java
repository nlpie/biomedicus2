package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section detector based off rules for clinical notes.
 *
 * @author Ben Knoll
 * @author Yan Wang (rules)
 * @since 1.4
 */
@DocumentScoped
public class RuleBasedSectionDetector implements DocumentProcessor {

    private final Document document;

    /**
     * The section title/headers pattern.
     */
    private final Pattern headers;

    /**
     * Injectable constructor.
     *
     * @param document the document to process.
     * @param headers the section title/headers pattern.
     */
    @Inject
    RuleBasedSectionDetector(Document document, @Named("sectionHeaders") Pattern headers) {
        this.document = document;
        this.headers = headers;
    }


    @Override
    public void process() throws BiomedicusException {
        String text = document.getText();
        Matcher matcher = headers.matcher(text);
        int prevBegin = 0;
        int prevEnd = 0;
        while (matcher.find()) {
            int begin = matcher.start();
            if (!text.substring(prevBegin, begin).isEmpty()) {
                document.createSection(Spans.spanning(prevBegin, begin))
                        .withContentStart(prevEnd)
                        .withSectionTitle(text.substring(prevBegin, prevEnd).trim())
                        .withHasSubsections(false)
                        .withLevel(0)
                        .build();
            }

            prevBegin = begin;
            prevEnd = matcher.end();
        }

        int textEnd = text.length();
        if (!text.substring(prevBegin, textEnd).isEmpty()) {
            document.createSection(Spans.spanning(prevBegin, textEnd))
                    .withContentStart(prevEnd)
                    .withSectionTitle(text.substring(prevBegin, prevEnd).trim())
                    .withHasSubsections(false)
                    .withLevel(0)
                    .build();
        }
    }
}
