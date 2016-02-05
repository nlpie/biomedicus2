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

import mockit.*;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.util.Progress;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 *
 */
public class FilesInDirectoryReaderTest {
    @Tested
    FilesInDirectoryReader filesInDirectoryReader;

    @Mocked Path path;

    @Mocked File file;

    @Mocked Stream<Path> pathStream;

    @Mocked Iterator<Path> pathIterator;

    @Mocked MockInputFileAdapter mockInputFileAdapter;

    @Mocked UimaContextAdmin uimaContext;

    @Mocked ProcessingResourceMetaData resourceMetaData;

    @Mocked FileInputStream fileInputStream;

    @Test
    public void testInitialize(@Mocked BiPredicate<Path, BasicFileAttributes> matcher) throws Exception {
        setField(filesInDirectoryReader, uimaContext);
        setField(filesInDirectoryReader, resourceMetaData);

        new Expectations() {{
            uimaContext.getConfigParameterValue("inputDirectory"); result = "aInputDir";
            uimaContext.getConfigParameterValue("recurseDepth"); result = 4;
            uimaContext.getConfigParameterValue("extensionGlob"); result = "glob*";
            uimaContext.getConfigParameterValue("targetViewName"); result = "targetView";
        }};

        new Expectations(Paths.class) {{
            Paths.get("aInputDir"); result = path;
        }};

        new Expectations(Files.class) {{
            Files.find(path, 4, withAny(matcher)); times = 2; result = pathStream;
        }};

        new Expectations() {{
            pathStream.count(); result = 200l; times = 1;
            pathStream.iterator(); result = pathIterator;
            uimaContext.getConfigParameterValue("inputFileAdapterClass"); result = MockInputFileAdapter.class.getCanonicalName();
            new MockInputFileAdapter(); result = mockInputFileAdapter;
        }};

        filesInDirectoryReader.initialize();

        new Verifications() {{
            Assert.assertEquals(200, (int) Deencapsulation.getField(filesInDirectoryReader, "totalFiles"));

            @SuppressWarnings("unchecked")
            Stream<Path> pathStreamField = Deencapsulation.getField(filesInDirectoryReader, "matchingFiles");
            Assert.assertEquals(pathStream, pathStreamField);

            @SuppressWarnings("unchecked")
            Iterator<Path> pathIteratorField = Deencapsulation.getField(filesInDirectoryReader, "filesIterator");
            Assert.assertEquals(pathIterator, pathIteratorField);

            mockInputFileAdapter.initialize(uimaContext, resourceMetaData);
            mockInputFileAdapter.setTargetView("targetView");
        }};
    }

    @Test
    public void testGetNext(@Mocked CAS aCAS) throws Exception {
        setField(filesInDirectoryReader, "completed", 0);
        setField(filesInDirectoryReader, pathIterator);
        setField(filesInDirectoryReader, mockInputFileAdapter);

        new Expectations() {{
            pathIterator.next(); result = path;
            path.toFile(); result = file;
            new FileInputStream(file); result = fileInputStream;
        }};

        filesInDirectoryReader.getNext(aCAS);

        Object completed = getField(filesInDirectoryReader, "completed");
        assertEquals(1, (int) completed);

        new VerificationsInOrder() {{
            mockInputFileAdapter.adaptFile(aCAS, path);
        }};
    }

    @Test
    public void testHasNext() throws Exception {
        setField(filesInDirectoryReader, pathIterator);

        new Expectations() {{
            pathIterator.hasNext(); result = false;
        }};

        assertFalse(filesInDirectoryReader.hasNext());
    }

    @Test
    public void testGetProgress() throws Exception {
        setField(filesInDirectoryReader, "completed", 20);
        setField(filesInDirectoryReader, "totalFiles", 200);

        Progress progress = filesInDirectoryReader.getProgress()[0];

        assertEquals(20, progress.getCompleted());
        assertEquals(200, progress.getTotal());
        assertEquals(false, progress.isApproximate());
    }

    @Test
    public void testClose() throws Exception {
        Deencapsulation.setField(filesInDirectoryReader, pathStream);

        filesInDirectoryReader.close();

        new FullVerificationsInOrder() {{
            pathStream.close();
        }};
    }
}