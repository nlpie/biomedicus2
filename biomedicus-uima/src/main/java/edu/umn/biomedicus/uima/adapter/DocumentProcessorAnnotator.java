package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.application.*;
import edu.umn.biomedicus.common.settings.Settings;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Runs a class of type {@link DocumentProcessor}, injecting it with the document data.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class DocumentProcessorAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String RESOURCE_GUICE_INJECTOR = "guiceInjector";

    public static final String PARAM_DOCUMENT_PROCESSOR = "documentProcessor";

    public static final String PARAM_VIEW_NAME = "viewName";

    public static final String PARAM_EAGER_LOAD = "eagerLoad";

    public static final List<String> KNOWN_PARAMS = Arrays.asList(PARAM_DOCUMENT_PROCESSOR, PARAM_VIEW_NAME, PARAM_EAGER_LOAD);

    private static final String PARAM_POST_PROCESSORS = "postProcessors";

    @Nullable
    private Injector injector;

    @Nullable
    private String viewName;

    @Nullable
    private Class<? extends DocumentProcessor> documentProcessorClass;

    @Nullable
    private ProcessorSettings processorSettings;

    @Nullable
    private String[] postProcessors;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        Settings.Builder settingsBuilder = Settings.builder();
        for (String parameterName : aContext.getConfigParameterNames()) {
            if (parameterName != null && !KNOWN_PARAMS.contains(parameterName)) {
                settingsBuilder.put(parameterName, aContext.getConfigParameterValue(parameterName));
            }
        }

        processorSettings = new UimaProcessorSettings(settingsBuilder.build());

        try {
            injector = ((GuiceInjector) aContext.getResourceObject(RESOURCE_GUICE_INJECTOR)).getInjector()
                    .createChildInjector(new ProcessorSettingsModule(processorSettings));
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }

        String[] eagerLoad = (String[]) aContext.getConfigParameterValue(PARAM_EAGER_LOAD);
        if (eagerLoad != null) {
            for (String className : eagerLoad) {
                try {
                    Object eagerLoaded = injector.getInstance(Class.forName(className));
                    if (eagerLoaded instanceof DataLoader) {
                        ((DataLoader) eagerLoaded).eagerLoad();
                    }
                } catch (ClassNotFoundException | BiomedicusException e) {
                    LOGGER.error("Error during document processor loading phase", e);
                    throw new ResourceInitializationException(e);
                }
            }
        }

        postProcessors = (String[]) aContext.getConfigParameterValue(PARAM_POST_PROCESSORS);

        String documentProcessorClassName = (String) aContext.getConfigParameterValue(PARAM_DOCUMENT_PROCESSOR);
        try {
            documentProcessorClass = Class.forName(documentProcessorClassName).asSubclass(DocumentProcessor.class);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        viewName = (String) aContext.getConfigParameterValue(PARAM_VIEW_NAME);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        assert injector != null;
        assert documentProcessorClass != null;
        JCas view;
        if (CAS.NAME_DEFAULT_SOFA.equals(viewName)) {
            view = aJCas;
        } else {
            try {
                view = aJCas.getView(viewName);
            } catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        Map<Key<?>, Object> seededObjects = new HashMap<>();
        seededObjects.put(Key.get(JCas.class), view);

        try {
            BiomedicusScopes.runInDocumentScope(() -> {
                injector.getInstance(documentProcessorClass).process();
                return null;
            }, seededObjects);
        } catch (Exception e) {
            LOGGER.error("Error during processing", e);
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        try {
            if (postProcessors == null) {
                return;
            }

            if (injector == null) {
                throw new AnalysisEngineProcessException();
            }

            for (String postProcessorClassName : postProcessors) {
                try {
                    Class<? extends PostProcessor> postProcessorClass = Class.forName(postProcessorClassName).asSubclass(PostProcessor.class);
                    PostProcessor postProcessor = injector.getInstance(postProcessorClass);
                    postProcessor.afterProcessing();
                } catch (ClassNotFoundException | BiomedicusException e) {
                    LOGGER.error("Error during post processing", e);
                    throw new AnalysisEngineProcessException(e);
                }
            }
        } catch (Throwable throwable) {
            // this is really ugly but the default UIMA CPE discards these errors without logging.
            LOGGER.error("Error during collectionProcessingComplete.", throwable);
            throw throwable;
        }
    }
}
