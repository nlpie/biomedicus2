package edu.umn.biomedicus.uima;

import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
public final class UimaHelpers {
    private UimaHelpers() {
        throw new UnsupportedOperationException("Instantiation of utility class");
    }

    public static CollectionReaderDescription loadCollectionReaderDescription(Path path) throws BiomedicusException {
        CollectionReaderDescription collectionReaderDescription;
        try {
            XMLInputSource aInput = new XMLInputSource(path.toFile());
            collectionReaderDescription = UIMAFramework.getXMLParser().parseCollectionReaderDescription(aInput);
        } catch (IOException | InvalidXMLException e) {
            throw new BiomedicusException(e);
        }
        return collectionReaderDescription;
    }
}
