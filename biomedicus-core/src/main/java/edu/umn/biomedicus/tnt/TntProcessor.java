package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Sentence;

import javax.inject.Inject;

/**
 *
 */
public class TntProcessor implements DocumentProcessor {
    private final Document document;

    private final TntPosTagger tntPosTagger;

    @Inject
    public TntProcessor(Document document, TntProcessorModel tntProcessorModel) {
        this.document = document;
        this.tntPosTagger = tntProcessorModel.getTntPosTagger();
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            tntPosTagger.tagSentence(sentence);
        }
    }
}
