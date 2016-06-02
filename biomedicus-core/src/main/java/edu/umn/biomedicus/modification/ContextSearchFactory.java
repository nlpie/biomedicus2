package edu.umn.biomedicus.modification;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.TermToken;

@DocumentScoped
public class ContextSearchFactory {
    private final Document document;
    private final Labels<TermToken> termTokenLabels;

    @Inject
    public ContextSearchFactory(Document document, Labels<TermToken> termTokenLabels) {
        this.document = document;
        this.termTokenLabels = termTokenLabels;
    }

    public ContextSearch create(ContextCues contextCues) {
        return new ContextSearch(contextCues, document, termTokenLabels);
    }
}
