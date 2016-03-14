package edu.umn.biomedicus.tools.newinfo;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.application.ProcessorSettings;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.files.FileNameProvider;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 */
public class NewInformationRtfRewriterDocumentProcessor implements DocumentProcessor {

    private static final Logger LOGGER = LogManager.getLogger();


    private final NewInformationRtfDocumentAppender newInformationRtfDocumentAppender;


    private final JCas jCas;


    private final Path outputDirectory;

    @Inject
    public NewInformationRtfRewriterDocumentProcessor(JCas jCas,
                                                      NewInformationRtfDocumentAppender newInformationRtfDocumentAppender,
                                                      ProcessorSettings processorSettings) {
        this.jCas = jCas;
        this.newInformationRtfDocumentAppender = newInformationRtfDocumentAppender;
        outputDirectory = processorSettings.getSettings().getAsPath("outputDirectory");
    }

    @Override
    public void process() throws BiomedicusException {
        String document = newInformationRtfDocumentAppender.modifyRtf(jCas);

        FileNameProvider fileNameProvider = FileNameProviders.fromInitialView(jCas, ".rtf");
        Path path = outputDirectory.resolve(fileNameProvider.getFileName());

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Writing rewritten RTF document to location: {}", path.toString());
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE)) {
            bufferedWriter.write(document);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
