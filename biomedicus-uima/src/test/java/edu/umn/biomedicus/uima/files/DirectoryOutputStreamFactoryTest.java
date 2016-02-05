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
import mockit.*;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link DirectoryOutputStreamFactory}.
 */
public class DirectoryOutputStreamFactoryTest {
    @Tested
    DirectoryOutputStreamFactory directoryOutputStreamFactory;

    @Injectable("extensionName") String extension;

    @Injectable Path outputDir, outputFilePath;

    @Injectable File file, outFile;

    @Mocked Paths paths;

    @Mocked
    FileNameProvider fileNameProvider;

    @Test
    public void testMakesOutputDir() throws Exception {
        new Expectations() {{
            outputDir.toFile(); result = file;
            file.exists(); result = false;
            file.mkdirs(); result = true;
        }};

        assertNotNull(directoryOutputStreamFactory.getPath(fileNameProvider));
    }

    @Test(expectedExceptions = BiomedicusException.class)
    public void testMakeOutputDirThrows() throws Exception {
        new Expectations() {{
            outputDir.toFile(); result = file;
            file.exists(); result = false;
            file.mkdirs(); result = false;
        }};

        directoryOutputStreamFactory.getPath(fileNameProvider);
    }

    @Test
    public void testGetsNameFromProvider() throws Exception {
        new Expectations() {{
            outputDir.toFile(); result = file;
            onInstance(file).exists(); result = true;
            fileNameProvider.getFileName(); result = "outputFile";
            outputDir.resolve("outputFile"); result = outputFilePath;
            outputFilePath.toFile(); result = outFile;
            onInstance(outFile).exists(); result = false;
        }};

        assertEquals(outputFilePath, directoryOutputStreamFactory.getPath(fileNameProvider));

        new Verifications() {{
            onInstance(outFile).delete(); times = 0;
        }};
    }

    @Test
    public void testDeletesExistingFile() throws Exception {
        new Expectations() {{
            outputDir.toFile(); result = file;
            onInstance(file).exists(); result = true;
            fileNameProvider.getFileName(); result = "outputFile";
            outputDir.resolve("outputFile"); result = outputFilePath;
            outputFilePath.toFile(); result = outFile;
            onInstance(outFile).exists(); result = true;
            onInstance(outFile).delete(); result = true;
        }};

        directoryOutputStreamFactory.getPath(fileNameProvider);
    }

    @Test(expectedExceptions = BiomedicusException.class)
    public void testThrowExceptionWhenFailToDeleteFile() throws Exception {
        new Expectations() {{
            outputDir.toFile(); result = file;
            onInstance(file).exists(); result = true;
            fileNameProvider.getFileName(); result = "outputFile";
            outputDir.resolve("outputFile"); result = outputFilePath;
            outputFilePath.toFile(); result = outFile;
            onInstance(outFile).exists(); result = true;
            onInstance(outFile).delete(); result = false;
        }};

        directoryOutputStreamFactory.getPath(fileNameProvider);
    }
}