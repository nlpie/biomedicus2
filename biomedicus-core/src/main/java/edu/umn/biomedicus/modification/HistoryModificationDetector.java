package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

@DocumentScoped
public class HistoryModificationDetector implements DocumentProcessor {
    private final HistoryModificationModel historyModificationModel;

    private final ContextSearchFactory contextSearchFactory;

    private final Document document;

    private final Labels<TermToken> termTokens;

    public HistoryModificationDetector(HistoryModificationModel historyModificationModel,
                                       ContextSearchFactory contextSearchFactory,
                                       Document document,
                                       Labels<TermToken> termTokens) {
        this.historyModificationModel = historyModificationModel;
        this.contextSearchFactory = contextSearchFactory;
        this.document = document;
        this.termTokens = termTokens;
    }

    @Override
    public void process() throws BiomedicusException {
        ContextSearch contextSearch = contextSearchFactory.create(historyModificationModel.getContextCues());

    }
}
