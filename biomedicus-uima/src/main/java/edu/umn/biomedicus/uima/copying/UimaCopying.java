package edu.umn.biomedicus.uima.copying;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

/**
 *
 */
public final class UimaCopying {
    private UimaCopying() {
        throw new UnsupportedOperationException("Instantiation of utility class");
    }

    public static void copyFeatureStructure(FeatureStructure featureStructure, CAS destinationView) {
        CAS source = featureStructure.getCAS();
        FeatureStructureCopyingQueue featureStructureCopyingQueue = new FeatureStructureCopyingQueue(source,
                destinationView);
        featureStructureCopyingQueue.enqueue(featureStructure);
        featureStructureCopyingQueue.run();
    }

    public static void copyFeatureStructuresOfType(String typeName, CAS sourceView, CAS destinationView) {
        FeatureStructureCopyingQueue featureStructureCopyingQueue = new FeatureStructureCopyingQueue(sourceView,
                destinationView);
        FSIterator<FeatureStructure> iterator = sourceView.getIndexRepository()
                .getAllIndexedFS(sourceView.getTypeSystem().getType(typeName));
        while (iterator.hasNext()) {
            FeatureStructure featureStructure = iterator.get();
            featureStructureCopyingQueue.enqueue(featureStructure);
        }
        featureStructureCopyingQueue.run();
    }

    public static void copyFeatureStructuresOfType(int type, JCas sourceView, JCas destinationView) {
        copyFeatureStructuresOfType(sourceView.getType(type).casType.getName(), sourceView.getCas(),
                destinationView.getCas());
    }
}
