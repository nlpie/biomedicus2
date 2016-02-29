package edu.umn.biomedicus.application;

import com.google.inject.Provider;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 *
 */
public abstract class ModelLoader<T> implements Provider<T> {
    private final Semaphore semaphore = new Semaphore(1);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private T instance;

    public void eagerLoad() throws BiomedicusException {
        load();
    }

    @Override
    public T get() {
        try {
            load();
        } catch (BiomedicusException e) {
            throw new IllegalStateException();
        }
        return instance;
    }

    private void load() throws BiomedicusException {
        if (semaphore.tryAcquire()) {
            instance = loadModel();
            countDownLatch.countDown();
        } else {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new BiomedicusException(e);
            }
        }
    }

    protected abstract T loadModel() throws BiomedicusException;
}
