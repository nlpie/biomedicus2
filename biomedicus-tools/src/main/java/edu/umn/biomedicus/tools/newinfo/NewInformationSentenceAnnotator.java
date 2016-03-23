package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.type.NewInformationAnnotation;
import edu.umn.biomedicus.uima.Views;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Annotates entire sentences for new information.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class NewInformationSentenceAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA parameter for the directory of new info files.
     */
    private static final String PARAM_NEW_INFO_DIRECTORY = "newInfoDirectory";

    /**
     * The directory of new information files.
     */
    @Nullable
    private Path newInfoDirectory;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        LOGGER.info("Initializing new information annotator.");

        newInfoDirectory = Paths.get((String) aContext.getConfigParameterValue(PARAM_NEW_INFO_DIRECTORY));
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        assert newInfoDirectory != null;
        LOGGER.debug("Processing new information output into annotations.");

        JCas systemView = Views.getSystemView(jCas);

        Document document = UimaAdapters.documentFromInitialView(jCas);

        Iterator<Sentence> sentenceIterator = NewInformationSentenceIterator.create(document);
        int currentSentence = -1;
        Sentence sentence = null;

        Path newInformationFile = newInfoDirectory.resolve(document.getIdentifier() + ".txt");

        if (Files.notExists(newInformationFile)) {
            LOGGER.warn("File does not exist: {}", newInformationFile.toString());
            return;
        }

        Set<String> labeledKinds = new HashSet<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(newInformationFile)) {
            Iterator<String[]> newInfoIterator = bufferedReader.lines()
                    .map(line -> line.split("\\t"))
                    .iterator();
            while (newInfoIterator.hasNext()) {
                String[] newInfo = newInfoIterator.next();
                int sentenceIndex = Integer.parseInt(newInfo[0]);

                while (currentSentence != sentenceIndex) {
                    sentence = sentenceIterator.next();
                    sentenceIndex++;
                    labeledKinds = new HashSet<>();
                }
                assert sentence != null;

                String kind = null;
                if (newInfo.length == 5) {
                    kind = newInfo[4];
                }
                if (!labeledKinds.contains(kind)) {
                    NewInformationAnnotation newInformationAnnotation = new NewInformationAnnotation(systemView,
                            sentence.getBegin(), sentence.getEnd());
                    newInformationAnnotation.setKind(kind);
                    newInformationAnnotation.addToIndexes();
                    labeledKinds.add(kind);
                }
            }
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
