package edu.umn.biomedicus.tools.newinfo;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.application.ProcessorSettings;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.concepts.SemanticTypeNetwork;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 *
 */
@DocumentScoped
public class NewInformationWriterDocumentProcessor implements DocumentProcessor {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Document document;

    private final NewInformationWriterFactory newInformationWriterFactory;

    @Inject
    public NewInformationWriterDocumentProcessor(Document document,
                                                 ProcessorSettings processorSettings,
                                                 SemanticTypeNetwork semanticTypeNetwork) throws BiomedicusException {
        this.document = document;
        Path outputDir = processorSettings.getSettings().getAsPath("outputDir");

        newInformationWriterFactory = NewInformationWriterFactory.createWithOutputDirectory(outputDir,
                semanticTypeNetwork);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Writing tokens, sentences and terms for new information for document.");
        newInformationWriterFactory.writeForDocument(document);
    }
}
