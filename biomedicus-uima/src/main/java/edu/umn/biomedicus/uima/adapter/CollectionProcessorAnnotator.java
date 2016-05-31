package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.application.CollectionProcessorRunner;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CollectionProcessorAnnotator extends JCasAnnotator_ImplBase {
    private static final List<String> KNOWN_PARAMETERS = Arrays.asList("collectionProcessor", "viewName", "eagerLoad", "postProcessors");

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProcessorAnnotator.class);

    @Nullable
    private Injector injector;

    @Nullable
    private String viewName;

    @Nullable
    private CollectionProcessorRunner collectionProcessorRunner;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        try {
            injector = ((GuiceInjector) aContext.getResourceObject("guiceInjector")).getInjector();
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }
        collectionProcessorRunner = new CollectionProcessorRunner(injector);

        String[] eagerLoad = (String[]) aContext.getConfigParameterValue("eagerLoad");
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

        try {
            String collectionProcessorClassName = (String) aContext.getConfigParameterValue("collectionProcessor");
            collectionProcessorRunner.setCollectionProcessorClassName(collectionProcessorClassName);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        Map<String, Object> settingsMap = new HashMap<>();
        for (String parameterName : aContext.getConfigParameterNames()) {
            if (parameterName != null && !KNOWN_PARAMETERS.contains(parameterName)) {
                settingsMap.put(parameterName, aContext.getConfigParameterValue(parameterName));
            }
        }
        try {
            collectionProcessorRunner.initialize(settingsMap);
        } catch (BiomedicusException e) {
            throw new ResourceInitializationException(e);
        }

        viewName = (String) aContext.getConfigParameterValue("viewName");
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        assert injector != null;
        assert collectionProcessorRunner != null;
        if (viewName == null) {
            throw new IllegalStateException("view name is null");
        }
        try {

            LOGGER.debug("Processing document from view: {}", viewName);
            JCas view = jCas.getView(viewName);
            if (view == null) {
                LOGGER.error("Trying to process null view");
                throw new BiomedicusException("View was null");
            }

            HashMap<Key<?>, Object> additionalSeeded = new HashMap<>();
            additionalSeeded.put(Key.get(JCas.class), view);
            additionalSeeded.put(Key.get(CAS.class), view.getCas());

            JCasDocument jCasDocument = new JCasDocument(view);
            collectionProcessorRunner.processDocument(jCasDocument, additionalSeeded);
        } catch (CASException e) {
            LOGGER.error("error loading cas from view: " + viewName, e);
            throw new AnalysisEngineProcessException(e);
        } catch (BiomedicusException e) {
            LOGGER.error("error while processing document");
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        assert collectionProcessorRunner != null;

        try {
            collectionProcessorRunner.finish();
        } catch (BiomedicusException e) {
            LOGGER.error("Failed collection processor after processing", e);
            throw new AnalysisEngineProcessException(e);
        }
    }
}
