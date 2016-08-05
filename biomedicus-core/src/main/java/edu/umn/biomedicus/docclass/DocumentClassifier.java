package edu.umn.biomedicus.docclass;

import com.google.inject.Inject;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;

/**
 * Classifies documents using a DocumentClassifierModel
 *
 * Created by gpfinley on 7/28/16.
 */
@DocumentScoped
public class DocumentClassifier implements DocumentProcessor {

    private final DocumentClassifierModel model;
    private final Document document;

    @Inject
    public DocumentClassifier(@Setting("docclass.model") DocumentClassifierModel model, Document document) throws BiomedicusException {
        this.model = model;
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        document.setMetadata(model.getMetadataKey(), model.predict(document));
    }

}
