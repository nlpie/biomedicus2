package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.application.BiomedicusScopes;
import edu.umn.biomedicus.common.settings.Settings;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.application.ProcessorSettings;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Runs a class of type {@link DocumentProcessor}, injecting it with the document data.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class DocumentProcessorAnnotator extends JCasAnnotator_ImplBase {
    public static final String RESOURCE_GUICE_INJECTOR = "guiceInjector";

    public static final String PARAM_DOCUMENT_PROCESSOR = "documentProcessor";

    public static final String PARAM_VIEW_NAME = "viewName";

    public static final String PARAM_EAGER_LOAD = "eagerLoad";

    @Nullable
    private Injector injector;

    @Nullable
    private String viewName;

    @Nullable
    private Class<? extends DocumentProcessor> documentProcessorClass;

    @Nullable
    private ProcessorSettings processorSettings;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        try {
            injector = ((GuiceInjector) aContext.getResourceObject(RESOURCE_GUICE_INJECTOR)).getInjector();
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }

        String[] eagerLoad = (String[]) aContext.getConfigParameterValue(PARAM_EAGER_LOAD);
        if (eagerLoad != null) {
            for (String className : eagerLoad) {
                try {
                    injector.getInstance(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }

        String documentProcessorClassName = (String) aContext.getConfigParameterValue(PARAM_DOCUMENT_PROCESSOR);
        try {
            documentProcessorClass = Class.forName(documentProcessorClassName).asSubclass(DocumentProcessor.class);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        Settings.Builder settingsBuilder = Settings.builder();
        for (String parameterName : aContext.getConfigParameterNames()) {
            if (parameterName != null && !Arrays.asList(PARAM_DOCUMENT_PROCESSOR, PARAM_VIEW_NAME, PARAM_EAGER_LOAD).contains(parameterName)) {
                settingsBuilder.put(parameterName, aContext.getConfigParameterValue(parameterName));
            }
        }

        processorSettings = new UimaProcessorSettings(settingsBuilder.build());

        viewName = (String) aContext.getConfigParameterValue(PARAM_VIEW_NAME);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        assert injector != null;
        assert documentProcessorClass != null;
        JCas view;
        try {
            view = aJCas.getView(viewName);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        Map<Key<?>, Object> seededObjects = new HashMap<>();
        seededObjects.put(Key.get(JCas.class), view);
        seededObjects.put(Key.get(ProcessorSettings.class),
                Objects.requireNonNull(processorSettings, "processor settings should be non-null"));

        try {
            BiomedicusScopes.runInDocumentScope(() -> {
                injector.getInstance(documentProcessorClass).process();
                return null;
            }, seededObjects);
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
