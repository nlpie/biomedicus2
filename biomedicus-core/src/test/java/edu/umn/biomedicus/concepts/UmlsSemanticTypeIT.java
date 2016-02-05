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

package edu.umn.biomedicus.concepts;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 */
public class UmlsSemanticTypeIT {
    @Test
    public void testSemGroups() throws Exception {
        try (
                InputStream semGroupsStream = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("edu/umn/biomedicus/concepts/SemGroups.txt");
                BufferedReader semGroupsReader = new BufferedReader(new InputStreamReader(semGroupsStream));
        ) {
            semGroupsReader.lines().forEach(line -> {
                String[] splits = line.split(Pattern.quote("|"));

                String groupId = splits[0];
                String groupName = splits[1];
                String tui = splits[2];
                String typeName = splits[3];

                UmlsSemanticType umlsSemanticType = UmlsSemanticType.forTui(tui);
                assertNotNull(umlsSemanticType, "Null semantic type for tui: " + tui);
                assertEquals(umlsSemanticType.toString(), typeName);
                UmlsSemanticTypeGroup group = umlsSemanticType.getGroup();
                assertNotNull(group);
                assertEquals(groupId, group.getIdentifier());
                assertEquals(groupName, group.toString());
            });
        }
    }
}