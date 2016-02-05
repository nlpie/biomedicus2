/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Reader which iterates files in a directory and uses an adapter to convert files into CAS documents for UIMA.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class FilesInDirectoryReader extends CollectionReader_ImplBase {
    /**
     * Class logger.
     */
    private final Logger LOGGER = LogManager.getLogger();

    /**
     * View to load data into.
     */
    public static final String PARAM_TARGET_VIEW_NAME = "targetViewName";

    /**
     * Name of configuration parameter that must be set to the path of a directory containing input
     * files.
     */
    public static final String PARAM_INPUT_DIR = "inputDirectory";

    /**
     * Name of configuration parameter that indicates the depth of recursion.
     */
    public static final String PARAM_RECURSE_DEPTH = "recurseDepth";

    /**
     * Name of configuration parameter for the extension glob to use to filter the files.
     */
    public static final String PARAM_EXTENSION_GLOB = "extensionGlob";

    /**
     * Name of the input file adapter class to use.
     */
    public static final String PARAM_INPUT_FILE_ADAPTER_CLASS = "inputFileAdapterClass";

    /**
     * Number of completed files.
     */
    private int completed;

    /**
     * Iterator of file paths.
     */
    @Nullable
    private Iterator<Path> filesIterator;

    /**
     * The stream of file paths.
     */
    @Nullable
    private Stream<Path> matchingFiles;

    /**
     * The number of total files.
     */
    private int totalFiles;

    /**
     * The input file adapter to use.
     */
    @Nullable
    private InputFileAdapter inputFileAdapter;

    /**
     * {@inheritDoc}
     *
     * Initializes the iterator over paths in the directory.
     *
     * @throws ResourceInitializationException
     */
    @Override
    public void initialize() throws ResourceInitializationException {
        super.initialize();

        LOGGER.info("Initializing reader directory stream");

        String inputDirectory = (String) getConfigParameterValue(PARAM_INPUT_DIR);

        int recurseDepth = (int) getConfigParameterValue(PARAM_RECURSE_DEPTH);

        Path inputDir = Paths.get(inputDirectory);
        LOGGER.debug("Running on inputPath: {}", inputDir.toAbsolutePath());

        String extensionGlob = (String) getConfigParameterValue(PARAM_EXTENSION_GLOB);

        BiPredicate<Path, BasicFileAttributes> filePredicate = (p, bfa) -> {
            PathMatcher matcher = inputDir.getFileSystem().getPathMatcher("glob:" + extensionGlob);
            return matcher.matches(p);
        };

        try {
            totalFiles = (int) Files.find(inputDir, recurseDepth, filePredicate).count();
            matchingFiles = Files.find(inputDir, recurseDepth, filePredicate);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        filesIterator = matchingFiles.iterator();

        completed = 0;

        String inputFileAdapterClassName = (String) getConfigParameterValue(PARAM_INPUT_FILE_ADAPTER_CLASS);

        try {
            inputFileAdapter = Class.forName(inputFileAdapterClassName)
                    .asSubclass(InputFileAdapter.class)
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        inputFileAdapter.initialize(getUimaContext(), getProcessingResourceMetaData());

        String targetViewName = (String) getConfigParameterValue(PARAM_TARGET_VIEW_NAME);
        if (targetViewName != null) {
            inputFileAdapter.setTargetView(targetViewName);
        }
    }

    /**
     * Gets the next cas, calls subclass's read method to perform the actual reading.
     *
     * @param aCAS cas to read data into
     * @throws CollectionException   if the subclass read fails
     * @throws IOException           if we fail to find the file
     */
    @Override
    public void getNext(CAS aCAS) throws CollectionException, IOException {
        Objects.requireNonNull(filesIterator);
        Objects.requireNonNull(inputFileAdapter);
        LOGGER.debug("Getting a file from the directory");

        Path next = filesIterator.next();

        LOGGER.info("Reading file: {}", next.getFileName());
        try (FileInputStream inputStream = new FileInputStream(next.toFile())) {
            inputFileAdapter.adaptFile(aCAS, next);
        }
        completed++;
        LOGGER.debug("Completed reading {} files", completed);
    }

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem) throws ResourceInitializationException {
        Objects.requireNonNull(inputFileAdapter);
        super.typeSystemInit(aTypeSystem);

        inputFileAdapter.initTypeSystem(aTypeSystem);
    }

    /**
     * {@inheritDoc}
     *
     * Returns if the directory has another file to read
     *
     * @return true if there are one or more files, false otherwise.
     */
    @Override
    public boolean hasNext() {
        Objects.requireNonNull(filesIterator);
        LOGGER.debug("Checking if there are any files remaining");

        return filesIterator.hasNext();
    }

    /**
     * {@inheritDoc}
     *
     * Returns an approximate progress in number of entities
     *
     * @return approximate progress
     */
    @Override
    public Progress[] getProgress() {
        LOGGER.trace("Progress: {} files completed", completed);
        return new Progress[]{new ProgressImpl(completed, totalFiles, Progress.ENTITIES, false)};
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        Objects.requireNonNull(matchingFiles);
        matchingFiles.close();
    }
}
