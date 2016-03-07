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
import edu.umn.biomedicus.uima.files.DirectoryOutputStreamFactory;
import edu.umn.biomedicus.uima.files.FileNameProvider;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import mockit.*;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Unit test for {@link NewInformationWriterFactory}.
 */
public class NewInformationWriterFactoryTest {
    @Tested NewInformationWriterFactory newInformationWriterFactory;

    @Injectable DirectoryOutputStreamFactory tokensDirectoryOSF;
    @Injectable DirectoryOutputStreamFactory termsDirectoryOSF;
    @Injectable DirectoryOutputStreamFactory sentencesDirectoryOSF;

    @Test
    public void testWriteForDocument(
            @Mocked Document document,
            @Mocked FileNameProvider fileNameProvider,
            @Mocked FileNameProviders fileNameProviders,
            @Mocked Path path,
            @Mocked Files files,
            @Mocked BufferedWriter bufferedWriter,
            @Mocked NewInformationWriter newInformationWriter,
            @Mocked NewInformationWriter.Builder newInfoBuilder,
            @Mocked TermsWriter termsWriter
    ) throws Exception {
        new Expectations() {{
            FileNameProviders.fromDocument(document, ".txt"); result = fileNameProvider;
            tokensDirectoryOSF.getPath(fileNameProvider); result = path;
            termsDirectoryOSF.getPath(fileNameProvider); result = path;
            sentencesDirectoryOSF.getPath(fileNameProvider); result = path;
            Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW); result = bufferedWriter; times = 3;
            NewInformationWriter.builder(); result = newInfoBuilder;
            newInfoBuilder.withSentencesWriter(bufferedWriter); result = newInfoBuilder;
            TermsWriter.forDocument(document, bufferedWriter); result = termsWriter;
            newInfoBuilder.withTermsWriter(termsWriter); result = newInfoBuilder;
            newInfoBuilder.withTokensWriter(bufferedWriter); result = newInfoBuilder;
            newInfoBuilder.withDocument(document); result = newInfoBuilder;
            newInfoBuilder.build(); result = newInformationWriter;
            newInformationWriter.hasNextSentence(); returns(true, true, false);
        }};

        newInformationWriterFactory.writeForDocument(document);

        new Verifications() {{
            newInformationWriter.writeNextSentence(); times = 2;
        }};
    }
}