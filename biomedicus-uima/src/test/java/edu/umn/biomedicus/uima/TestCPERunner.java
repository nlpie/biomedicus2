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

package edu.umn.biomedicus.uima;

import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 */
public class TestCPERunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCPERunner.class);

    private final CollectionProcessingEngine collectionProcessingEngine;

    public TestCPERunner(CollectionProcessingEngine collectionProcessingEngine) {
        this.collectionProcessingEngine = collectionProcessingEngine;
    }

    public static TestCPERunner create(CpeDescription cpeDescription) throws BiomedicusException {
        try {
            return new TestCPERunner(UIMAFramework.produceCollectionProcessingEngine(cpeDescription));
        } catch (ResourceInitializationException e) {
            throw new BiomedicusException(e);
        }
    }

    public static CpeDescription createDescription(String descriptorPath) throws BiomedicusException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(descriptorPath);
        XMLInputSource xmlInputSource = new XMLInputSource(resourceAsStream, Paths.get("").toFile());
        try {
            return UIMAFramework.getXMLParser().parseCpeDescription(xmlInputSource);
        } catch (InvalidXMLException e) {
            throw new BiomedicusException(e);
        }
    }

    public static TestCPERunner create(String descriptorPath) throws BiomedicusException {
        return create(createDescription(descriptorPath));
    }

    public List<Exception> run() throws BiomedicusException {
        final List<Exception> exceptionList = new ArrayList<>();
        final Semaphore semaphore = new Semaphore(0);

        collectionProcessingEngine.addStatusCallbackListener(new StatusCallbackListener() {
            @Override
            public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
                LOGGER.info(aStatus.getStatusMessage());

                for (String s : aStatus.getFailedComponentNames()) {
                    LOGGER.error("Failed component: {}", s);
                }


                for (Exception exception : aStatus.getExceptions()) {
                    LOGGER.error("Exception", exception);
                    exceptionList.add(exception);
                }
                LOGGER.info("Entity processing complete");
            }

            @Override
            public void initializationComplete() {

            }

            @Override
            public void batchProcessComplete() {

            }

            @Override
            public void collectionProcessComplete() {

                LOGGER.info("Collection processing complete");
                semaphore.release();
            }

            @Override
            public void paused() {

            }

            @Override
            public void resumed() {

            }

            @Override
            public void aborted() {

            }
        });

        try {
            collectionProcessingEngine.process();
        } catch (ResourceInitializationException e) {
            throw new BiomedicusException(e);
        }
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new BiomedicusException(e);
        }

        return exceptionList;
    }
}
