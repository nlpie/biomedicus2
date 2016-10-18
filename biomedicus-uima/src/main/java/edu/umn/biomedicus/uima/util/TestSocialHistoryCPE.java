
package edu.umn.biomedicus.uima.util;




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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * Created by wang2258 on 9/14/16.
 */


/**
 * <p>Class was adapted from the UIMA examples file org.apache.uima.examples.cpe.SimpleRunCPE</p>
 * <p>
 * Main Class that runs a Collection Processing Engine (CPE). This class reads a CPE Descriptor as a
 * command-line argument and instantiates the CPE. It also registers a callback listener with the
 * CPE, which will print progress and statistics to System.out.
 * </p>
 *
 * @author Yan Wang
 * @since 1.0.0
 */
public class TestSocialHistoryCPE {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSocialHistoryCPE.class);


    private TestSocialHistoryCPE() {

    }

    /**
     * main class.
     *
     * @param args Command line arguments - see class description
     */
    public static void main(String[] args) {

        String descriptorPath = "/Users/wang2258/Projects/Tasks/20160404BiomedicusSH/testSH_preprocessed20161019.xml";

        long startTime = System.currentTimeMillis();


        // parse CPE descriptor
        LOGGER.info("Parsing CPE Descriptor");
        CpeDescription cpeDesc = null;
        try {
            cpeDesc = UIMAFramework.getXMLParser().parseCpeDescription(new XMLInputSource(descriptorPath));
        } catch (InvalidXMLException | IOException e) {
            LOGGER.error("Error parsing descriptor", e);
            System.exit(1);
        }
        // instantiate CPE
        LOGGER.info("Instantiating CPE");
        CollectionProcessingEngine collectionProcessingEngine = null;
        try {
            collectionProcessingEngine = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
        } catch (ResourceInitializationException e) {
            LOGGER.error("Error producing CPE", e);
            System.exit(1);
        }

        // Create and register a Status Callback Listener
        StatusCallbackListenerImpl statusCallbackListener = new StatusCallbackListenerImpl(startTime, collectionProcessingEngine);
        collectionProcessingEngine.addStatusCallbackListener(statusCallbackListener);

        // Start Processing
        LOGGER.info("Running CPE");
        try {
            collectionProcessingEngine.process();
        } catch (ResourceInitializationException e) {
            LOGGER.error("Error processing CPE", e);
            System.exit(1);
        }

        try {
            statusCallbackListener.waitForCompletion();
        } catch (InterruptedException e) {
            LOGGER.error("Error waiting for CPE completion", e);
            System.exit(1);
        }

        LOGGER.info("Completed running CPE");

        System.exit(0);
    }

    /**
     * Callback Listener. Receives event notifications from CPE.
     */
    private static class StatusCallbackListenerImpl implements StatusCallbackListener {
        private final long startTime;

        private final CollectionProcessingEngine collectionProcessingEngine;

        private final Semaphore completionSemaphore = new Semaphore(0);

        private long initCompleteTime;

        private int entityCount = 0;

        private long size = 0;


        public StatusCallbackListenerImpl(long startTime, CollectionProcessingEngine collectionProcessingEngine) {
            this.startTime = startTime;
            this.collectionProcessingEngine = collectionProcessingEngine;
        }

        /**
         * Called when the initialization is completed.
         *
         * @see StatusCallbackListener#initializationComplete()
         */
        @Override
        public void initializationComplete() {
            LOGGER.info("CPM Initialization Complete");
            initCompleteTime = System.currentTimeMillis();
        }

        /**
         * Called when the batchProcessing is completed.
         *
         * @see StatusCallbackListener#batchProcessComplete()
         */
        @Override
        public void batchProcessComplete() {
            LOGGER.info("Completed " + entityCount + " documents");
            if (size > 0) {
                LOGGER.info("; " + size + " characters");
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Time Elapsed : " + elapsedTime + " ms ");
        }

        /**
         * Called when the collection processing is completed.
         *
         * @see StatusCallbackListener#collectionProcessComplete()
         */
        @Override
        public void collectionProcessComplete() {
            long time = System.currentTimeMillis();
            LOGGER.info("Completed " + entityCount + " documents");
            if (size > 0) {
                LOGGER.info("; " + size + " characters");
            }
            long initTime = initCompleteTime - startTime;
            long processingTime = time - initCompleteTime;
            long elapsedTime = initTime + processingTime;
            LOGGER.info("Total Time Elapsed: " + elapsedTime + " ms ");
            LOGGER.info("Initialization Time: " + initTime + " ms");
            LOGGER.info("Processing Time: " + processingTime + " ms");
            LOGGER.info("PERFORMANCE REPORT \n" + collectionProcessingEngine.getPerformanceReport().toString());

            completionSemaphore.release();
        }

        /**
         * Called when the CPM is paused.
         *
         * @see StatusCallbackListener#paused()
         */
        @Override
        public void paused() {
            LOGGER.info("Paused");
        }

        /**
         * Called when the CPM is resumed after a pause.
         *
         * @see StatusCallbackListener#resumed()
         */
        @Override
        public void resumed() {
            LOGGER.info("Resumed");
        }

        /**
         * Called when the CPM is stopped abruptly due to errors.
         *
         * @see StatusCallbackListener#aborted()
         */
        @Override
        public void aborted() {
            LOGGER.error("CPE processing was aborted");

            completionSemaphore.release();
        }

        /**
         * Called when the processing of a Document is completed. <br>
         * The process status can be looked at and corresponding actions taken.
         *
         * @param aCas    CAS corresponding to the completed processing
         * @param aStatus EntityProcessStatus that holds the status of all the events for aEntity
         */
        @Override
        public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
            List<Exception> exceptions = aStatus.getExceptions();
            LOGGER.debug(aStatus.getStatusMessage());

            for (Exception exception : exceptions) {
                LOGGER.error("Exception processing a CAS: ", exception);
            }
            if (exceptions.size() > 0) {
                throw new RuntimeException("Processing exception");
            }


            entityCount++;
            String docText = aCas.getDocumentText();
            if (docText != null) {
                size += docText.length();
            }
        }

        public void waitForCompletion() throws InterruptedException {
            completionSemaphore.acquire();
        }
    }
}
