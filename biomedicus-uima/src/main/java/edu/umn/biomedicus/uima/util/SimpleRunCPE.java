/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import javax.annotation.Nullable;
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

/**
 * <p>Class was adapted from the UIMA examples file org.apache.uima.examples.cpe.SimpleRunCPE</p>
 * <p>
 * Main Class that runs a Collection Processing Engine (CPE). This class reads a CPE Descriptor as a
 * command-line argument and instantiates the CPE. It also registers a callback listener with the
 * CPE, which will print progress and statistics to System.out.
 * </p>
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SimpleRunCPE implements Callable<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRunCPE.class);

  private final CollectionProcessingEngine collectionProcessingEngine;

  private final Semaphore completionSemaphore = new Semaphore(0);

  private int entityCount = 0;

  private long size = 0;

  private final List<Exception> exceptions = new ArrayList<>();

  @Nullable
  private Consumer<CAS> casConsumer = null;


  public SimpleRunCPE(CpeDescription cpeDesc) throws ResourceInitializationException {
    LOGGER.info("Instantiating CPE");
    collectionProcessingEngine = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
    collectionProcessingEngine.addStatusCallbackListener(new StatusCallbackListener() {

      @Override
      public void initializationComplete() {
        LOGGER.info("CPM Initialization Complete");
      }

      @Override
      public void batchProcessComplete() {
        LOGGER.info("Completed " + entityCount + " documents");
        if (size > 0) {
          LOGGER.info("; " + size + " characters");
        }
      }

      @Override
      public void collectionProcessComplete() {
        LOGGER.info("Completed " + entityCount + " documents");
        if (size > 0) {
          LOGGER.info("; " + size + " characters");
        }
        LOGGER.info(
            "PERFORMANCE REPORT \n" + collectionProcessingEngine.getPerformanceReport().toString());

        completionSemaphore.release();
      }

      @Override
      public void paused() {
        LOGGER.info("Paused");
      }

      @Override
      public void resumed() {
        LOGGER.info("Resumed");
      }

      @Override
      public void aborted() {
        LOGGER.error("CPE processing was aborted");

        completionSemaphore.release();
      }

      @Override
      public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
        exceptions.addAll(aStatus.getExceptions());
        LOGGER.debug(aStatus.getStatusMessage());

        if (casConsumer != null) {
          casConsumer.accept(aCas);
        }

        for (Exception exception : exceptions) {
          LOGGER.error("Exception processing a CAS: ", exception);
        }

        entityCount++;
        String docText = aCas.getDocumentText();
        if (docText != null) {
          size += docText.length();
        }
      }
    });
  }

  public void setCasConsumer(@Nullable Consumer<CAS> casConsumer) {
    this.casConsumer = casConsumer;
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }

  /**
   * main class.
   *
   * @param args Command line arguments - see class description
   */
  public static void main(String[] args) {
    String descriptorPath = args[0];

    if (args.length < 1) {
      System.out.print(" Arguments to the program are as follows : \n"
          + "args[0] : path to CPE descriptor file");
      System.exit(1);
    }

    CpeDescription cpeDesc = null;
    try {
      cpeDesc = UIMAFramework.getXMLParser()
          .parseCpeDescription(new XMLInputSource(descriptorPath));
    } catch (InvalidXMLException | IOException e) {
      System.err.print("Error parsing descriptor");
      System.exit(1);
    }

    SimpleRunCPE simpleRunCPE = null;
    try {
      simpleRunCPE = new SimpleRunCPE(cpeDesc);
      simpleRunCPE.runCPE();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      simpleRunCPE.waitForCompletion();
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }

    if (simpleRunCPE.exceptions.size() > 0) {
      System.exit(1);
    }

    System.exit(0);
  }

  public void runCPE() throws ResourceInitializationException {
    LOGGER.info("Running CPE");
    collectionProcessingEngine.process();
  }

  public void waitForCompletion() throws InterruptedException {
    completionSemaphore.acquire();
  }

  @Override
  public Void call() throws Exception {
    runCPE();
    waitForCompletion();
    return null;
  }
}
