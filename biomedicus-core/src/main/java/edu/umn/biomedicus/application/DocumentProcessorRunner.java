package edu.umn.biomedicus.application;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@ProcessorScoped
class DocumentProcessorRunner implements CollectionProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Injector injector;

    private final String documentProcessorClassName;

    @Inject
    DocumentProcessorRunner(Injector injector,
                            @ProcessorSetting("documentProcessor") String documentProcessorClassName) {
        this.injector = injector;
        this.documentProcessorClassName = documentProcessorClassName;
    }

    @Override
    public void processDocument(Document document) throws BiomedicusException {
        try {
            Map<Key<?>, Object> seededObjects = new HashMap<>();
            seededObjects.put(Key.get(Document.class), document);
            BiomedicusScopes.runInDocumentScope(() -> {
                Class<? extends DocumentProcessor> aClass = Class.forName(documentProcessorClassName)
                        .asSubclass(DocumentProcessor.class);
                injector.getInstance(aClass).process();
                return null;
            }, seededObjects);
        } catch (Exception e) {
            LOGGER.error("Error during processing", e);
            throw new BiomedicusException(e);
        }
    }

    @Override
    public void allDocumentsProcessed() throws BiomedicusException {

    }
}
