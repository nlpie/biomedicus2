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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.concepts.SemanticTypeNetwork;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.files.DirectoryOutputStreamFactory;
import edu.umn.biomedicus.uima.files.FileNameProvider;
import edu.umn.biomedicus.uima.files.FileNameProviders;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Responsible for creating new {@link NewInformationWriter} classes, and then writing the documents' data using that
 * class.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class NewInformationWriterFactory {
    /**
     * OutputStreamFactory for writing the tokens files.
     */
    private final DirectoryOutputStreamFactory tokensDirectoryOSF;

    /**
     * OutputStreamFactory for writing the terms files.
     */
    private final DirectoryOutputStreamFactory termsDirectoryOSF;

    /**
     * OutputStreamFactory for writing the sentences files.
     */
    private final DirectoryOutputStreamFactory sentencesDirectoryOSF;

    private final SemanticTypeNetwork semanticTypeNetwork;

    /**
     * Private constructor. Initializes the different factories for the output streams to files.
     *
     * @param tokensDirectoryOSF    factory for the directory of token files output streams
     * @param termsDirectoryOSF     factory for the directory of term files output streams
     * @param sentencesDirectoryOSF factory for the directory of sentence files output streams.
     * @param semanticTypeNetwork
     */
    protected NewInformationWriterFactory(DirectoryOutputStreamFactory tokensDirectoryOSF,
                                          DirectoryOutputStreamFactory termsDirectoryOSF,
                                          DirectoryOutputStreamFactory sentencesDirectoryOSF, SemanticTypeNetwork semanticTypeNetwork) {
        this.tokensDirectoryOSF = tokensDirectoryOSF;
        this.termsDirectoryOSF = termsDirectoryOSF;
        this.sentencesDirectoryOSF = sentencesDirectoryOSF;
        this.semanticTypeNetwork = semanticTypeNetwork;
    }

    /**
     * Static factory method. Creates a single directory and makes the 3 subdirectories for tokens, terms, and
     * sentences.
     *
     * @param outputDir the output directory path
     * @return a {@code NewInformationWriterFactory} which outputs to the designated folder
     * @throws BiomedicusException if it fails to create the subdirectories.
     */
    static NewInformationWriterFactory createWithOutputDirectory(Path outputDir, SemanticTypeNetwork semanticTypeNetwork) throws BiomedicusException {
        DirectoryOutputStreamFactory tokensDirectoryOSF = new DirectoryOutputStreamFactory(outputDir.resolve("tokens"));
        DirectoryOutputStreamFactory termsDirectoryOSF = new DirectoryOutputStreamFactory(outputDir.resolve("terms"));
        DirectoryOutputStreamFactory sentencesDirectoryOSF = new DirectoryOutputStreamFactory(outputDir.resolve("sentences"));

        return new NewInformationWriterFactory(tokensDirectoryOSF, termsDirectoryOSF, sentencesDirectoryOSF, semanticTypeNetwork);
    }

    /**
     * Writes the document's terms, tokens, and sentences to the directories specified in construction.
     *
     * @param document the document to write
     * @throws BiomedicusException if we fail in writing.
     */
    void writeForDocument(Document document) throws BiomedicusException {
        FileNameProvider fileNameProvider = FileNameProviders.fromDocument(document, ".txt");

        Path tokensPath = tokensDirectoryOSF.getPath(fileNameProvider);
        Path termsPath = termsDirectoryOSF.getPath(fileNameProvider);
        Path sentencesPath = sentencesDirectoryOSF.getPath(fileNameProvider);
        try (
                BufferedWriter tokensWriter = Files.newBufferedWriter(tokensPath, StandardOpenOption.CREATE_NEW);
                BufferedWriter termsBufferedWriter = Files.newBufferedWriter(termsPath, StandardOpenOption.CREATE_NEW);
                BufferedWriter sentencesWriter = Files.newBufferedWriter(sentencesPath, StandardOpenOption.CREATE_NEW)
        ) {

            NewInformationWriter newInformationWriter = NewInformationWriter.builder()
                    .withSentencesWriter(sentencesWriter)
                    .withTermsWriter(TermsWriter.forDocument(document, termsBufferedWriter, semanticTypeNetwork))
                    .withTokensWriter(tokensWriter)
                    .withDocument(document)
                    .build();

            while (newInformationWriter.hasNextSentence()) {
                newInformationWriter.writeNextSentence();
            }
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
