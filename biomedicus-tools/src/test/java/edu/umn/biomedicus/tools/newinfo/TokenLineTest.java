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

import mockit.Injectable;
import mockit.Tested;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link TokenLine}.
 */
public class TokenLineTest {
    @Tested TokenLine tokenLine;

    @Injectable(value = "0") int sentenceNumber;

    @Injectable(value = "0") int wordNumber;

    @Injectable(value = "aToken\t\n") String word;

    @Test
    public void testLine() throws Exception {
        String line = tokenLine.line();

        Assert.assertEquals("0\t0\taToken\\t\\n\n", line);
    }
}