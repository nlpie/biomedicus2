package edu.umn.biomedicus.modification;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

@ProvidedBy(HistoryModificationModel.Loader.class)
class HistoryModificationModel {
    private final ContextCues contextCues;

    private HistoryModificationModel(ContextCues contextCues) {
        this.contextCues = contextCues;
    }

    public ContextCues getContextCues() {
        return contextCues;
    }

    static class Loader extends DataLoader<HistoryModificationModel> {
        @Override
        protected HistoryModificationModel loadModel() throws BiomedicusException {
            return null;
        }
    }
}
