package edu.umn.biomedicus.uima;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 *
 */
public class NoDocumentsCollectionReader extends CollectionReader_ImplBase {
    @Override
    public void getNext(CAS cas) throws IOException, CollectionException {
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return false;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }

    @Override
    public void close() throws IOException {

    }
}
