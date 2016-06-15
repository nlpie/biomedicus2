/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.application;

import com.google.inject.Provider;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Loads data into, and functions as a provider for, a singleton object.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public abstract class DataLoader<T> implements Provider<T> {
    /**
     * Prevents the data from being loaded more than once.
     */
    private final Semaphore loadOnce = new Semaphore(1);

    /**
     * Lets additional threads that attempt to load during loading wait for the data to finish loading.
     */
    private final CountDownLatch waitTilLoaded = new CountDownLatch(1);

    /**
     * true when the object has been loaded, single check to prevent double semaphore + latch check.
     */
    private boolean loaded = false;

    /**
     * The singleton instance of the object to provide.
     */
    private T instance;

    /**
     * Loads the data before it is actually needed.
     *
     * @throws BiomedicusException
     */
    public void eagerLoad() throws BiomedicusException {
        if (!loaded) {
            load();
        }
    }

    @Override
    public T get() {
        if (!loaded) {
            try {
                load();
            } catch (BiomedicusException e) {
                throw new IllegalStateException(e);
            }
        }
        return instance;
    }

    private void load() throws BiomedicusException {
        if (loadOnce.tryAcquire()) {
            instance = loadModel();
            loaded = true;
            waitTilLoaded.countDown();
        } else {
            try {
                waitTilLoaded.await();
            } catch (InterruptedException e) {
                throw new BiomedicusException(e);
            }
        }
    }

    /**
     * To be implemented by subclasses, performs the initialization of the object.
     *
     * @return full initialized object.
     * @throws BiomedicusException
     */
    protected abstract T loadModel() throws BiomedicusException;
}
