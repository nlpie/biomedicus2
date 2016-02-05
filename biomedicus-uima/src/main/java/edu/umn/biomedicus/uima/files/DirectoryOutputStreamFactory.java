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

import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Creates output streams based on a cas's MetaData documentId;
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class DirectoryOutputStreamFactory {
    /**
     * The directory to place created files.
     */
    private final Path outputDir;

    /**
     * Creates the output stream factory
     *
     * @param outputDir output directory to use
     * @throws BiomedicusException if the directory does not exist and cannot be created.
     */
    public DirectoryOutputStreamFactory(Path outputDir) throws BiomedicusException {
        this.outputDir = outputDir;
    }

    /**
     * Creates an output stream using the view's MetaData object in the system view.
     *
     * @param fileNameProvider provides the file name
     * @return a new, open OutputStream. The caller is responsible for closing the output stream.
     * @throws BiomedicusException if we fail to get the system view, or create the output stream.
     */
    public Path getPath(FileNameProvider fileNameProvider) throws BiomedicusException {
        File outputDirFile = outputDir.toFile();
        if (!outputDirFile.exists()) {
            boolean mkdirs = outputDirFile.mkdirs();
            if (!mkdirs) {
                throw new BiomedicusException("Output directory does not exist and could not create");
            }
        }

        Path outFilePath = outputDir.resolve(fileNameProvider.getFileName());
        File outFile = outFilePath.toFile();
        if (outFile.exists()) {
            boolean delete = outFile.delete();
            if (!delete) {
                throw new BiomedicusException("Could not delete existing file: " + outFilePath.toString());
            }
        }
        return Objects.requireNonNull(outFilePath);
    }


}
