package edu.umn.biomedicus.writers;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@DocumentScoped
public class PtbTagsWriter implements DocumentProcessor {

    private final Path outputDir;
    private final Document document;

    @Inject
    public PtbTagsWriter(@ProcessorSetting("writer.ptbTags.outputDir.path") Path outputDir, Document document) {
        this.outputDir = outputDir;
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        String text = document.getText();
        StringBuilder rewriter = new StringBuilder(text);
        int added = 0;
        for (Token token : document.getTokens()) {
            int end = token.getEnd() + added;
            String insertion = "/" + token.getPartOfSpeech().toString();
            rewriter.insert(end, insertion);
            if (rewriter.charAt(end + insertion.length()) != ' ') {
                rewriter.insert(end + insertion.length(), ' ');
                added += 1;
            }
            added += insertion.length();
        }

        try {
            Path fileName = outputDir.resolve(document.getIdentifier());
            Files.write(fileName, rewriter.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
